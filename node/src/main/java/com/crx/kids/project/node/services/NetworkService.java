package com.crx.kids.project.node.services;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.CriticalSectionToken;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.utils.NetUtil;
import com.crx.kids.project.node.utils.RoutingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Autowired
    private BootstrapService bootstrapService;

//    @Autowired
//    private RoutingService routingService;

    @Autowired
    private NodeGateway nodeGateway;

    @EventListener(ApplicationReadyEvent.class)
    public void connectToBootstrap() {
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn(Configuration.bootstrap, Configuration.myself);

        if (checkInResponseOptional.isPresent()) {
            logger.info("CheckIn response {}", checkInResponseOptional.get());

            Configuration.id = checkInResponseOptional.get().getId();
            Network.maxNodeInSystem = Configuration.id;

            Network.firstKnownNode = checkInResponseOptional.get().getNodeInfo();

            // join network,
            if (Network.firstKnownNode != null) {
                int receiver = RoutingUtils.darah(Configuration.id);
                NewbieJoinMessage newbieJoinMessage = new NewbieJoinMessage(Configuration.id, receiver, Configuration.myself);
                // instead of calculating next hop, next hop is firsKnownNode

                Result joinResult = nodeGateway.send(newbieJoinMessage, Network.firstKnownNode, Methods.NEWBIE_JOIN);

                if (joinResult.isError()) {
                    logger.error("Received error response. Exiting.");
                    System.exit(0);
                }
            }
        }
        else {
            logger.error("Shutting down application. No response from bootstrap.");
            System.exit(0);
        }

        if (Configuration.id == 1) {
            CriticalSection.token = new CriticalSectionToken();
            CriticalSection.token.getQueue();
            CriticalSection.token.getSuzukiKasamiNodeMap();
            CriticalSection.tokenIdle.set(true);
            logger.info("TOKEND ASSIGNED TO NODE 1");
        }

        logger.info("Communication with bootstrap finished.");
    }


    public void newbieJoin(NewbieJoinMessage newbieJoinMessage) {
        logger.info("Received newbie join message {}", newbieJoinMessage);

        // message is for me!
        logger.info("Local handling newbie join message {}", newbieJoinMessage);

        NewbieAcceptedMessage newbieAcceptedMessage = new NewbieAcceptedMessage();
        newbieAcceptedMessage.setSender(Configuration.id);
        newbieAcceptedMessage.setReceiver(newbieJoinMessage.getSender());
        newbieAcceptedMessage.setSecondNeighbour(new FullNodeInfo(Configuration.id, Configuration.myself));

        if (Network.neighbours.size() > 0) {
            Network.configurationLock.readLock().lock();
            try {
                // even numbers are connected to second smallest neighbour
                if (newbieJoinMessage.getSender() % 2 == 0) {
                    newbieAcceptedMessage.setFirstNeighbour(Network.secondSmallestNeighbour);
                } else { // odd numbers are connected to smallest neighbour
                    newbieAcceptedMessage.setFirstNeighbour(Network.firstSmallestNeighbour);
                }
            } finally {
                Network.configurationLock.readLock().unlock();
            }
        }
        else {
            newbieAcceptedMessage.setFirstNeighbour(new FullNodeInfo(Configuration.id, Configuration.myself));
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(newbieJoinMessage.getNewbie(), Methods.NEWBIE_ACCEPTED), newbieAcceptedMessage, CommonResponse.class);
        logger.info("Response {}", response);
    }


    public void newbieAccepted(NewbieAcceptedMessage newbieAcceptedMessage) {
        logger.info("Newbie accepted with two neighbours {} and {}", newbieAcceptedMessage.getFirstNeighbour(), newbieAcceptedMessage.getSecondNeighbour());

        Network.configurationLock.writeLock().lock();
        try {
            Network.neighbours.put(newbieAcceptedMessage.getFirstNeighbour().getId(), newbieAcceptedMessage.getFirstNeighbour());
            Network.neighbours.put(newbieAcceptedMessage.getSecondNeighbour().getId(), newbieAcceptedMessage.getSecondNeighbour());

            Network.firstSmallestNeighbour = newbieAcceptedMessage.getFirstNeighbour().getId() < newbieAcceptedMessage.getSecondNeighbour().getId() ? newbieAcceptedMessage.getFirstNeighbour() : newbieAcceptedMessage.getSecondNeighbour();
            Network.secondSmallestNeighbour = newbieAcceptedMessage.getFirstNeighbour().getId() > newbieAcceptedMessage.getSecondNeighbour().getId() ? newbieAcceptedMessage.getFirstNeighbour() : newbieAcceptedMessage.getSecondNeighbour();
        }
        finally {
            Network.configurationLock.writeLock().unlock();
        }

        Network.neighbours.forEach( (receiver, nodeInfo) -> {
            AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, receiver, false, Configuration.myself);
            nodeGateway.send(alterRoutingTableMessage, nodeInfo, Methods.ALTER_NEIGHBOURS);
        });
    }

    public void alterRoutingTable(AlterRoutingTableMessage alterRoutingTableMessage) {
        logger.info("Altering routing table with {} : {}", alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());

        FullNodeInfo newNode = new FullNodeInfo(alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());

        Network.configurationLock.writeLock().lock();
        try {
            if (Network.firstSmallestNeighbour == null) {
                Network.firstSmallestNeighbour = newNode;
                logger.info("Set 1st smallest neighbour {}", newNode);

            }
            if (Network.secondSmallestNeighbour == null) {
                Network.secondSmallestNeighbour = newNode;
                logger.info("Set 2nd smallest neighbour {}", newNode);
            }

            if (newNode.getId() == Network.firstSmallestNeighbour.getId()) {
                Network.firstSmallestNeighbour = newNode;
                logger.info("Replaced 1st smallest neighbour {}", newNode);
            }
            else if (newNode.getId() == Network.secondSmallestNeighbour.getId()) {
                Network.secondSmallestNeighbour = newNode;
                logger.info("Replaced 2nd smallest neighbour {}", newNode);
            }

            if (newNode.getId() < Network.firstSmallestNeighbour.getId()) {
                Network.secondSmallestNeighbour = Network.firstSmallestNeighbour;
                Network.firstSmallestNeighbour = newNode;
                logger.info("Replaced 1st and 2nd smallest nodes {}", newNode);
            }
            else if (newNode.getId() < Network.secondSmallestNeighbour.getId()) {
                Network.firstSmallestNeighbour = newNode;
                logger.info("Replaced 2nd smallest node {}", newNode);
            }

            logger.info("1st = {},  2nd = {}", Network.firstSmallestNeighbour, Network.secondSmallestNeighbour);

            Network.neighbours.put(newNode.getId(), newNode);
        }
        finally {
            Network.configurationLock.writeLock().unlock();
        }
    }
}

package com.crx.kids.project.node.net;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.common.util.Error;
import com.crx.kids.project.common.util.ErrorCode;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.Configuration;
import com.crx.kids.project.node.ThreadUtil;
import com.crx.kids.project.node.bootstrap.BootstrapService;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.Trace;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.routing.RoutingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Autowired
    private BootstrapService bootstrapService;

    @Autowired
    private RoutingService routingService;

    @EventListener(ApplicationReadyEvent.class)
    public void connectToBootstrap() {
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn(Configuration.bootstrap, Configuration.myself);

        if (checkInResponseOptional.isPresent()) {
            logger.info("CheckIn response {}", checkInResponseOptional.get());

            Configuration.id = checkInResponseOptional.get().getId();
            Network.firstKnownNode = checkInResponseOptional.get().getNodeInfo();


            // join network,
            if (Network.firstKnownNode != null) {
                int receiver = RoutingUtils.darah(Configuration.id);
                NewbieJoinMessage newbieJoinMessage = new NewbieJoinMessage(Configuration.id, receiver, Configuration.myself);
                // instead of calculating next hop, next hop is firsKnownNode

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(Network.firstKnownNode, "node/net/newbie-join"), newbieJoinMessage, CommonResponse.class);
                    logger.info(response.toString());
                    CommonResponse commonResponse = response.getBody();
                    if (commonResponse.getType() == CommonType.OK) {
                        logger.info("Received ok response. Waiting for routing table.");
                    } else {
                        logger.info("Received error response. Exiting. {}", response);
                    }
                } catch (Exception e) {
                    logger.error("Shutting down application. Unable to connect to network.", e);
                    System.exit(0);
                }
            }
        }
        else {
            logger.error("Shutting down application. No response from bootstrap.");
            System.exit(0);
        }

        logger.info("Communication with bootstrap finished.");
    }


    @Async
    public void newbieJoinAsync(NewbieJoinMessage newbieJoinMessage) {
        logger.info("Received newbie join message {}", newbieJoinMessage);

        if (newbieJoinMessage.getReceiver() == Configuration.id) {
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
            ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(newbieJoinMessage.getNewbie(), "node/net/newbie-accepted"), newbieAcceptedMessage, CommonResponse.class);
            logger.info("Response {}", response);
        }
        else { // dispatch message
            newbieJoinMessage.addTrace(new Trace(Configuration.id, null));

            // loop until next hop is determined
            FullNodeInfo nextHop = ThreadUtil.loopWithResult(300, (l) -> {
                Optional<FullNodeInfo> optionalNextHop = routingService.nextHop(Configuration.id, newbieJoinMessage.getReceiver(), () -> Network.neighbours);
                if (optionalNextHop.isPresent()) {
                    l.stop();
                    return optionalNextHop.get();
                }
                logger.warn("Unable to find next hope. Sleep for 300 ms!");
                return null;
            });

            Result<Void> result = dispatchJoinRequest(nextHop, newbieJoinMessage);
            // TODO: // watch result type, if type is for logout, then remove node from routing table, and redispatch message!
        }
    }


    private Result<Void> dispatchJoinRequest(NodeInfo nextHop, NewbieJoinMessage newbieJoinMessage) {
        logger.info("Dispatching newbie join message {} to {}", newbieJoinMessage, nextHop);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(nextHop, "node/net/newbie-join"), newbieJoinMessage, CommonResponse.class);

            logger.info("Response {}", response);
            return Result.of(null);
        }
        catch (Exception e) {
            logger.error("",e);
            return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, e.getMessage()));
        }
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
            sendAlterRoutingMessage(nodeInfo, alterRoutingTableMessage);
        });
    }

    public Result<Void> sendAlterRoutingMessage(NodeInfo nextHop, AlterRoutingTableMessage alterRoutingTableMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(nextHop, "node/net/neighbours"), alterRoutingTableMessage, CommonResponse.class);

            logger.info("Alter routing response {}", response);
            return Result.of(null);
        }
        catch (Exception e) {
            logger.error("",e);
            return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, e.getMessage()));
        }
    }

    public Result<Void> alterRoutingTable(AlterRoutingTableMessage alterRoutingTableMessage) {
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
        return Result.of(null);
    }
}

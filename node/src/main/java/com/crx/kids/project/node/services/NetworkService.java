package com.crx.kids.project.node.services;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.*;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.CriticalSectionToken;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.GhostMessage;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.utils.RoutingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Autowired
    private BootstrapService bootstrapService;

//    @Autowired
//    private RoutingService routingService;

    @Autowired
    private NodeGateway nodeGateway;

    @Autowired
    private JobService jobService;

    @Autowired
    private CriticalSectionService criticalSectionService;

    @EventListener(ApplicationReadyEvent.class)
    public void connectToBootstrap() { // this is only done on host Ghost
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn(Node.bootstrap, Node.myself);

        Ghost host = new Ghost();



        if (checkInResponseOptional.isPresent()) {
            logger.info("CheckIn response {}", checkInResponseOptional.get());

            host.getConfiguration().setId(checkInResponseOptional.get().getId());

            host.getNetwork().getMaxNodeInSystem().set(host.getConfiguration().getId());

            host.getNetwork().setFirstKnownNode(checkInResponseOptional.get().getNodeInfo());

            // join network,
            if (host.getNetwork().getFirstKnownNode() != null) {
                int receiver = RoutingUtils.darah(host.getConfiguration().getId());
                NewbieJoinMessage newbieJoinMessage = new NewbieJoinMessage(host.getConfiguration().getId(), receiver, Node.myself);
                // instead of calculating next hop, next hop is firsKnownNode

                Result joinResult = nodeGateway.send(newbieJoinMessage, host.getNetwork().getFirstKnownNode(), Methods.NEWBIE_JOIN);

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

        if (host.getConfiguration().getId() == 1) {
            host.getCriticalSection().setToken(new CriticalSectionToken());
            host.getCriticalSection().getToken().getQueue();
            host.getCriticalSection().getToken().getSuzukiKasamiNodeMap();
            host.getCriticalSection().getTokenIdle().set(true);
            logger.info("TOKEND ASSIGNED TO NODE 1");
        }

        logger.info("Communication with bootstrap finished.");
    }


    public void newbieJoin(Ghost ghost, NewbieJoinMessage newbieJoinMessage) {
        logger.info("Received newbie join message {}", newbieJoinMessage);

        // message is for me!
        logger.info("Local handling newbie join message {}", newbieJoinMessage);

        NewbieAcceptedMessage newbieAcceptedMessage = new NewbieAcceptedMessage();
        newbieAcceptedMessage.setSender(ghost.getConfiguration().getId());
        newbieAcceptedMessage.setReceiver(newbieJoinMessage.getSender());
        newbieAcceptedMessage.setSecondNeighbour(new FullNodeInfo(ghost.getConfiguration().getId(), Node.myself));

        if (ghost.getNetwork().getRoutingTable().size() > 0) {
            ghost.getNetwork().configurationLock.readLock().lock();
            try {
                // even numbers are connected to second smallest neighbour
                if (newbieJoinMessage.getSender() % 2 == 0) {
                    newbieAcceptedMessage.setFirstNeighbour(ghost.getNetwork().getSecondSmallestNeighbour());
                } else { // odd numbers are connected to smallest neighbour
                    newbieAcceptedMessage.setFirstNeighbour(ghost.getNetwork().getFirstSmallestNeighbour());
                }
            } finally {
                ghost.getNetwork().configurationLock.readLock().unlock();
            }
        }
        else {
            newbieAcceptedMessage.setFirstNeighbour(new FullNodeInfo(ghost.getConfiguration().getId(), Node.myself));
        }

        newbieAcceptedMessage.setCurrentlyActiveDim(ghost.getJobs().getCurrentActiveDimension().get());
        newbieAcceptedMessage.setCollectedResults(ghost.getJobs().getCollectedResultsByDimensions());
        newbieAcceptedMessage.setFinishedJobs(ghost.getJobs().getFinishedJobs());

        Result result = nodeGateway.send(newbieAcceptedMessage, newbieJoinMessage.getNewbie(), Methods.NEWBIE_ACCEPTED);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(newbieJoinMessage.getNewbie(), Methods.NEWBIE_ACCEPTED), newbieAcceptedMessage, CommonResponse.class);
        logger.info("Response {}", result);
    }


    public void newbieAccepted(Ghost ghost, NewbieAcceptedMessage newbieAcceptedMessage) {
        logger.info("Newbie accepted with two neighbours {} and {}", newbieAcceptedMessage.getFirstNeighbour(), newbieAcceptedMessage.getSecondNeighbour());

        ghost.getNetwork().configurationLock.writeLock().lock();
        try {
            ghost.getNetwork().getRoutingTable().put(newbieAcceptedMessage.getFirstNeighbour().getId(), newbieAcceptedMessage.getFirstNeighbour());
            ghost.getNetwork().getRoutingTable().put(newbieAcceptedMessage.getSecondNeighbour().getId(), newbieAcceptedMessage.getSecondNeighbour());

            ghost.getNetwork().setFirstSmallestNeighbour(newbieAcceptedMessage.getFirstNeighbour().getId() < newbieAcceptedMessage.getSecondNeighbour().getId() ? newbieAcceptedMessage.getFirstNeighbour() : newbieAcceptedMessage.getSecondNeighbour());
            ghost.getNetwork().setSecondSmallestNeighbour(newbieAcceptedMessage.getFirstNeighbour().getId() > newbieAcceptedMessage.getSecondNeighbour().getId() ? newbieAcceptedMessage.getFirstNeighbour() : newbieAcceptedMessage.getSecondNeighbour());
        }
        finally {
            ghost.getNetwork().configurationLock.writeLock().unlock();
        }

        newbieAcceptedMessage.getCollectedResults().forEach((dim, jobMap) -> {
            ghost.getJobs().getCollectedResultsByDimensions().put(dim, new ConcurrentHashMap<>());
            Map<Integer, List<Integer[]>> jobs = ghost.getJobs().getCollectedResultsByDimensions().get(dim);

            jobMap.forEach((jobId, results) -> {
                jobs.put(jobId, results);
            });
        });

//        Jobs.currentActiveDim.set(newbieAcceptedMessage.getCurrentlyActiveDim());
        ghost.getJobs().getFinishedJobs().addAll(newbieAcceptedMessage.getFinishedJobs());

        if (newbieAcceptedMessage.getCurrentlyActiveDim() != -1) {
            jobService.startWorkForDimension(ghost, newbieAcceptedMessage.getCurrentlyActiveDim());
        }

        ghost.getNetwork().getRoutingTable().forEach( (receiver, nodeInfo) -> {
            AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(ghost.getConfiguration().getId(), receiver, false, Node.myself);
            nodeGateway.send(alterRoutingTableMessage, nodeInfo, Methods.ALTER_NEIGHBOURS);
        });
    }

    public void alterRoutingTable(Ghost ghost, AlterRoutingTableMessage alterRoutingTableMessage) {
        logger.info("Altering routing table with {} : {}", alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());

        FullNodeInfo newNode = new FullNodeInfo(alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());

        ghost.getNetwork().configurationLock.writeLock().lock();
        try {
            if (ghost.getNetwork().getFirstSmallestNeighbour() == null) {
                ghost.getNetwork().setFirstSmallestNeighbour(newNode);
                logger.info("Set 1st smallest neighbour {}", newNode);

            }
            if (ghost.getNetwork().getSecondSmallestNeighbour() == null) {
                ghost.getNetwork().setSecondSmallestNeighbour(newNode);
                logger.info("Set 2nd smallest neighbour {}", newNode);
            }

            if (newNode.getId() == ghost.getNetwork().getFirstSmallestNeighbour().getId()) {
                ghost.getNetwork().setFirstSmallestNeighbour(newNode);
                logger.info("Replaced 1st smallest neighbour {}", newNode);
            }
            else if (newNode.getId() == ghost.getNetwork().getSecondSmallestNeighbour().getId()) {
                ghost.getNetwork().setSecondSmallestNeighbour(newNode);
                logger.info("Replaced 2nd smallest neighbour {}", newNode);
            }

            if (newNode.getId() < ghost.getNetwork().getFirstSmallestNeighbour().getId()) {
                ghost.getNetwork().setSecondSmallestNeighbour(ghost.getNetwork().getFirstSmallestNeighbour());
                ghost.getNetwork().setFirstSmallestNeighbour(newNode);
                logger.info("Replaced 1st and 2nd smallest nodes {}", newNode);
            }
            else if (newNode.getId() < ghost.getNetwork().getSecondSmallestNeighbour().getId()) {
                ghost.getNetwork().setSecondSmallestNeighbour(newNode);
                logger.info("Replaced 2nd smallest node {}", newNode); // TODO: Check if first smalles should be set
            }

            logger.info("1st = {},  2nd = {}", ghost.getNetwork().getFirstSmallestNeighbour(), ghost.getNetwork().getSecondSmallestNeighbour());

            ghost.getNetwork().getRoutingTable().put(newNode.getId(), newNode);
        }
        finally {
            ghost.getNetwork().configurationLock.writeLock().unlock();
        }
    }

    public void handleHostRequest(GhostMessage ghostMessage) {
        logger.info("Received ghost message {}", ghostMessage);

        criticalSectionService.submitProcedureForCriticalExecution(token -> {
            if (ghostMessage.getStoppedJob() != -1) {
                jobService.initiateJobForDimension(ghostMessage.getStoppedJob());
            }
        });


        ghostMessage.getUnfinishedJobsForDimension().forEach((dim, jobQueue) -> {
            Jobs.jobsByDimensions.putIfAbsent(dim, new ConcurrentLinkedQueue<>());
            Queue<QueensJob> localJobQueue = Jobs.jobsByDimensions.get(dim);

            // consuming queue
            while (!jobQueue.isEmpty()) {
                localJobQueue.add(jobQueue.poll());
            }
        });

        logger.info("Routing table: {}", ghostMessage.getRoutingTable());

        Network.ghostRoutingTables.put(ghostMessage.getSender(), ghostMessage.getRoutingTable());
    }
}

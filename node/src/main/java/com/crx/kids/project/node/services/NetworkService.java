package com.crx.kids.project.node.services;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.CriticalSectionToken;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.HostMessage;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.utils.RoutingUtils;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Autowired
    private RoutingService routingService;

    @EventListener(ApplicationReadyEvent.class)
    public void connectToBootstrap() {
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn(Configuration.bootstrap, Configuration.myself);

        if (checkInResponseOptional.isPresent()) {
            logger.info("CheckIn response {}", checkInResponseOptional.get());

            Configuration.id = checkInResponseOptional.get().getId();
            Network.maxNodeInSystem.set(Configuration.id);

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
                    newbieAcceptedMessage.setFirstNeighbour(Network.secondSmallestNeighbour());
                } else { // odd numbers are connected to smallest neighbour
                    newbieAcceptedMessage.setFirstNeighbour(Network.firstSmallestNeighbour());
                }
            } finally {
                Network.configurationLock.readLock().unlock();
            }
        }
        else {
            newbieAcceptedMessage.setFirstNeighbour(new FullNodeInfo(Configuration.id, Configuration.myself));
        }

        newbieAcceptedMessage.setCurrentlyActiveDim(Jobs.currentActiveDim.get());
        newbieAcceptedMessage.setCollectedResults(Jobs.collectedResultsByDimensions);
        newbieAcceptedMessage.setFinishedJobs(Jobs.finishedJobs);

        Result result = nodeGateway.send(newbieAcceptedMessage, newbieJoinMessage.getNewbie(), Methods.NEWBIE_ACCEPTED);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(newbieJoinMessage.getNewbie(), Methods.NEWBIE_ACCEPTED), newbieAcceptedMessage, CommonResponse.class);
        logger.info("Response {}", result);
    }


    public void newbieAccepted(NewbieAcceptedMessage newbieAcceptedMessage) {
        logger.info("Newbie accepted with two neighbours {} and {}", newbieAcceptedMessage.getFirstNeighbour(), newbieAcceptedMessage.getSecondNeighbour());

        Network.neighbours.put(newbieAcceptedMessage.getFirstNeighbour().getId(), newbieAcceptedMessage.getFirstNeighbour());
        Network.neighbours.put(newbieAcceptedMessage.getSecondNeighbour().getId(), newbieAcceptedMessage.getSecondNeighbour());

        newbieAcceptedMessage.getCollectedResults().forEach((dim, jobMap) -> {
            Jobs.collectedResultsByDimensions.put(dim, new ConcurrentHashMap<>());
            Map<Integer, List<Integer[]>> jobs = Jobs.collectedResultsByDimensions.get(dim);

            jobMap.forEach((jobId, results) -> {
                jobs.put(jobId, results);
            });
        });

//        Jobs.currentActiveDim.set(newbieAcceptedMessage.getCurrentlyActiveDim());
        Jobs.finishedJobs.addAll(newbieAcceptedMessage.getFinishedJobs());

        if (newbieAcceptedMessage.getCurrentlyActiveDim() != -1) {
            jobService.startWorkForDimension(newbieAcceptedMessage.getCurrentlyActiveDim());
        }

        Network.neighbours.forEach( (receiver, nodeInfo) -> {
            AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, receiver, Configuration.myself);
            nodeGateway.send(alterRoutingTableMessage, nodeInfo, Methods.ALTER_NEIGHBOURS);
        });
    }


    public static final AtomicBoolean lock = new AtomicBoolean(true);

    public void handleHostRequest(HostMessage hostMessage) {

        lock.set(true);

        logger.info("Received ghost message {}", hostMessage);

        int oldHostId = Configuration.id;
//        Configuration.id = hostMessage.getSender();



        logger.info("HOST: Broadcast leave message with {}", oldHostId);
        Network.maxNodeInSystem.decrementAndGet();
        BroadcastMessage<String> leaveBroadcast = new BroadcastMessage<>(oldHostId, UUID.randomUUID().toString());
        routingService.broadcastMessage(leaveBroadcast, Methods.BROADCAST_LEAVE);

        logger.info("HOST: Updating results");
        hostMessage.getUnfinishedJobsForDimension().forEach((dim, jobQueue) -> {
            Jobs.jobsByDimensions.putIfAbsent(dim, new ConcurrentLinkedQueue<>());
            Queue<QueensJob> localJobQueue = Jobs.jobsByDimensions.get(dim);

            // consuming queue
            while (!jobQueue.isEmpty()) {
                localJobQueue.add(jobQueue.poll());
            }
        });

        while (lock.get()) {
            logger.info("waiting for ack from node {}", hostMessage.getSender());
            ThreadUtil.sleep(500);
        }

        // set routing table
        logger.info("HOST: Routing table setup");

        // remove myself
        hostMessage.getRoutingTable().remove(oldHostId);
        Network.neighbours.clear();
        Network.neighbours.putAll(hostMessage.getRoutingTable());
        Configuration.id = hostMessage.getSender();
        logger.info("HOST: Routing table: {}", hostMessage.getRoutingTable());
        logger.info("HOST: NEW ID {}", Configuration.id);

        logger.info("HOST: Sending alter routing table.");
        Network.neighbours.forEach((id, nodeInfo) -> {
            AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, id, Configuration.myself);
            nodeGateway.send(alterRoutingTableMessage, nodeInfo, Methods.ALTER_NEIGHBOURS);
        });

        criticalSectionService.submitProcedureForCriticalExecution(token -> {
            if (hostMessage.getStoppedJob() != -1) {
                jobService.initiateJobForDimension(hostMessage.getStoppedJob());
            }
        });
    }

    public void ack() {
        lock.set(false);
    }
}

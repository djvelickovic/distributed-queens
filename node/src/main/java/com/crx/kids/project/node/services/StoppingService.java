package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.messages.*;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class StoppingService {

    private static final Logger logger = LoggerFactory.getLogger(StoppingService.class);

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final AtomicBoolean ack = new AtomicBoolean(false);

    @Autowired
    private CriticalSectionService criticalSectionService;

    @Autowired
    private JobService jobService;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private BootstrapService bootstrapService;

    @Autowired
    private NodeGateway nodeGateway;


    public void initiateStoppingPrcedure() {

        Optional<FullNodeInfo> bestNeighbourOptional = bestNeighbour();

        if (!bestNeighbourOptional.isPresent()) {
            System.exit(0);
            return;
        }



        criticalSectionService.submitProcedureForCriticalExecution(token -> {
            bootstrapService.lock();

            int replacement = Network.maxNodeInSystem.get();

            logger.info("STOPPING: Chosen node {}", replacement);

            int activeJob = Jobs.currentActiveDim.get();

            logger.info("STOPPING: Pausing jobs");
            jobService.pause();

            logger.info("STOPPING: Broadcasting calculated results for unfinished jobs");
            jobService.broadcastCalculatedResultsForAllUnfinishedJobs();

            int criticalSectionReceiver;

            if (replacement == Configuration.id) { // its me.
                logger.info("STOPPING: I am the last node {}", replacement);

                logger.info("STOPPING: Broadcasting leave");

                BroadcastMessage<String> leaveBroadcast = new BroadcastMessage<>(Configuration.id, UUID.randomUUID().toString());
                routingService.broadcastMessage(leaveBroadcast, Methods.BROADCAST_LEAVE);

                logger.info("STOPPING: Alter routing table to neighbours.");

                Network.neighbours.forEach((id, nodeInfo) -> {
                    AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, id, true, Configuration.myself);
                    nodeGateway.send(alterRoutingTableMessage, nodeInfo, Methods.ALTER_NEIGHBOURS);
                });

                int rndNode = new Random().nextInt(Configuration.id-1)+1;
                logger.info("STOPPING: Chosen rnd node from system to resume finished jobs.");


                MaxLeaveMessage maxLeaveMessage = new MaxLeaveMessage(Configuration.id, rndNode, activeJob);
                routingService.dispatchMessage(maxLeaveMessage, Methods.MAX_LEAVE);

                criticalSectionReceiver = rndNode;
            }
            else {

                logger.info("STOPPING: Sending host request to {}", replacement);
                HostMessage hostMessage = new HostMessage(Configuration.id, replacement, activeJob, Network.neighbours, Jobs.jobsByDimensions, Configuration.myself,CriticalSection.suzukiKasamiCounter.get());
                routingService.dispatchMessage(hostMessage, Methods.HOST_REQUEST);


                while (Network.maxNodeInSystem.get() >= replacement) {
                    logger.info("STOPPING: waiting for broadcast message. max: {}, chosen: {}", Network.maxNodeInSystem.get(), replacement);
                    ThreadUtil.sleep(500);
                }

                PingMessage pingMessage = new PingMessage(Configuration.id, replacement);
                routingService.dispatchMessage(pingMessage, Methods.HOST_ACK);

//            logger.info("STOPPING: Sending alter routing table message to neighbours");
//
//            Network.neighbours.forEach((id, nodeInfo) -> { // nodeId / 2 + 1 will be notified that I m ghost!
//                FullNodeInfo fakeNodeInfo = new FullNodeInfo(Configuration.id, hostNode);
//                AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, id, true, fakeNodeInfo);
//                routingService.dispatchMessageNonAsync(alterRoutingTableMessage, Methods.ALTER_NEIGHBOURS);
//            });

                // waiting for confirmation

//            while (!ack.get()) {
//                logger.info("STOPPING: Waiting for ack message form {}", replacement);
//                ThreadUtil.sleep(500);
//            }

                criticalSectionReceiver = Configuration.id;
            }

            logger.info("STOPPING: Checking out from bootstrap");
            bootstrapService.unlock();
            bootstrapService.checkOut();

            executor.submit(() -> {
                logger.info("STOPPING: Entering safe stopping sleep.");
                ThreadUtil.sleep(2000);
                CriticalSection.token.getSuzukiKasamiNodeMap().remove(replacement);
                SuzukiKasamiTokenMessage suzukiKasamiTokenMessage = new SuzukiKasamiTokenMessage(Configuration.id, criticalSectionReceiver, CriticalSection.token);
                routingService.dispatchMessage(suzukiKasamiTokenMessage, Methods.CRITICAL_SECTION_TOKEN);
                logger.info("STOPPING: Stopping service");
                System.exit(0);
            });
        });

        // choose appropriate neighbour -> n
        // pause -> critical section

        // copy routing table to neighbour

        // broadcast results
        // send unfinished jobs to neighbour

        // alter routing tables to neighbours -> node does not exist

        // release critical section and send token to neighbour

        // notify bootstrap
    }


    private Optional<FullNodeInfo> bestNeighbour() {
        return Network.neighbours.entrySet().stream()
                .reduce((e1, e2) -> e1.getKey() > e2.getKey() ? e1 : e2)
                .map(e -> new FullNodeInfo(e.getKey(), e.getValue()));
    }


    public void handleAck(PingMessage pingMessage) {
        logger.info("Received ack from {}", pingMessage);
        ack.set(true);
    }
}

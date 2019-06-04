package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.HostMessage;
import com.crx.kids.project.node.messages.PingMessage;
import com.crx.kids.project.node.messages.SuzukiKasamiTokenMessage;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
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


    public void initiateStoppingPrcedure() {

        Optional<FullNodeInfo> bestNeighbourOptional = bestNeighbour();

        if (!bestNeighbourOptional.isPresent()) {
            // TODO: Node is alone in system
            return;
        }




//        FullNodeInfo hostNode = bestNeighbourOptional.get();




        criticalSectionService.submitProcedureForCriticalExecution(token -> {
            bootstrapService.lock();

            int replacement = Network.maxNodeInSystem.get();

            logger.info("STOPPING: Chosen node {}", replacement);

            if (replacement == Configuration.id) { // its 1, than just notify bootstrap and exit
                // TODO: bootstrap, exit
            }

            int activeJob = Jobs.currentActiveDim.get();

            logger.info("STOPPING: Pausing jobs");
            jobService.pause();

            logger.info("STOPPING: Broadcasting calculated results for unfinished jobs");
            jobService.broadcastCalculatedResultsForAllUnfinishedJobs();


            logger.info("STOPPING: Sending host request to {}", replacement);
            HostMessage hostMessage = new HostMessage(Configuration.id, replacement, activeJob, Network.neighbours, Jobs.jobsByDimensions, Configuration.myself);
            routingService.dispatchMessage(hostMessage, Methods.HOST_REQUEST);


            while (Network.maxNodeInSystem.get() >= replacement) {
                logger.info("STOPPING: waiting for broadcast message. max: {}, chosen: {}", Network.maxNodeInSystem.get(), replacement);
                ThreadUtil.sleep(500);
            }

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

            logger.info("STOPPING: Checking out from bootstrap");
            bootstrapService.unlock();
            bootstrapService.checkOut();

            executor.submit(() -> {
                logger.info("STOPPING: Entering safe stopping sleep.");
                ThreadUtil.sleep(5000);
                SuzukiKasamiTokenMessage suzukiKasamiTokenMessage = new SuzukiKasamiTokenMessage(Configuration.id, Configuration.id, CriticalSection.token);
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

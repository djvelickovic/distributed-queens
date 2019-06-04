package com.crx.kids.project.node.services;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.GhostMessage;
import com.crx.kids.project.node.messages.Message;
import com.crx.kids.project.node.utils.NetUtil;
import com.crx.kids.project.node.utils.RoutingUtils;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class StoppingService {

    private static final Logger logger = LoggerFactory.getLogger(StoppingService.class);

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

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


        FullNodeInfo hostNode = bestNeighbourOptional.get();

        logger.info("STOPPING: Chosen node {}", hostNode);

        if (hostNode.getId() == Configuration.id) { // its 1, than just notify bootstrap and exit
            // TODO: bootstrap, exit
        }


        criticalSectionService.submitProcedureForCriticalExecution(token -> {
            int activeJob = Jobs.currentActiveDim.get();

            logger.info("STOPPING: Pausing jobs {}", hostNode);
            jobService.pause();

            logger.info("STOPPING: Sending host request to {}", hostNode);

            GhostMessage ghostMessage = new GhostMessage(Configuration.id, hostNode.getId(), activeJob, Network.neighbours, Jobs.jobsByDimensions);
            routingService.dispatchMessageNonAsync(ghostMessage, Methods.HOST_REQUEST);


            logger.info("STOPPING: Broadcasting calculated results for unfinished jobs");

            jobService.broadcastCalculatedResultsForAllUnfinishedJobs();


            logger.info("STOPPING: Sending alter routing table message to neighbours");

            Network.neighbours.forEach((id, nodeInfo) -> { // nodeId / 2 + 1 will be notified that I m ghost!
                AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, id, true, hostNode);
                routingService.dispatchMessageNonAsync(alterRoutingTableMessage, Methods.ALTER_NEIGHBOURS);
            });

            logger.info("STOPPING: Checking out from bootstrap");
            bootstrapService.checkOut();

            executor.submit(() -> {
                logger.info("STOPPING: Entering for sure stopping sleep.");
                ThreadUtil.sleep(5000);
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








}

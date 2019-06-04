package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Ghost;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.messages.JobStealingMessage;
import com.crx.kids.project.node.messages.StolenJobsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class JobStealingService {
    private static final Logger logger = LoggerFactory.getLogger(JobStealingService.class);



    @Autowired
    private RoutingService routingService;

    @Autowired
    private QueensService queensService;

    public void addStolenJobs(Ghost ghost, int from, int dimension, List<QueensJob> stolenJobs) {
        logger.info("Collected {} Stolen Jobs from {}, for dim {}. Jobs {}", stolenJobs.size(), from, dimension, stolenJobs.toString());
        ghost.getJobs().getAskedNodesByDimension().get(dimension).add(from);
        ghost.getJobs().getStolenJobsByDimension().get(dimension).addAll(stolenJobs);
    }


    public Optional<Integer> getRandomUnaskedNode(Ghost ghost, int dimension) {

        Random rnd = new Random();

        Set<Integer> askedNodes = ghost.getJobs().getAskedNodesByDimension().get(dimension);
        askedNodes.add(ghost.getConfiguration().getId());

        while (true) {
            int genId = rnd.nextInt(ghost.getNetwork().getMaxNodeInSystem().get()) + 1;
            if (!askedNodes.contains(genId)) {
                return Optional.of(genId);
            }
            if (askedNodes.size() >= ghost.getNetwork().getMaxNodeInSystem().get()) {
                return Optional.empty();
            }
        }
    }

    public Optional<Queue<QueensJob>> stealJobs(Ghost ghost, int dimension) {
        ghost.getJobs().getAskedNodesByDimension().putIfAbsent(dimension, ConcurrentHashMap.newKeySet());
        ghost.getJobs().getStolenJobsByDimension().putIfAbsent(dimension, new ConcurrentLinkedQueue<>());
//        asnweredNodesByDimension.putIfAbsent(dimension, ConcurrentHashMap.newKeySet());

        Optional<Integer> rndUnaskedNode = getRandomUnaskedNode(ghost, dimension);

        if (!rndUnaskedNode.isPresent()) {
            logger.info("All nodes are asked for jobs for dimension {}", dimension);
            return Optional.empty();
        }

        logger.info("Stealing from {} for dimension {}", rndUnaskedNode.get(), dimension);

        JobStealingMessage jobStealingMessage = new JobStealingMessage(ghost.getConfiguration().getId(), rndUnaskedNode.get(), dimension);

        routingService.dispatchMessage(ghost, jobStealingMessage, Methods.JOB_STEALING_REQUEST);

        Set<Integer> answeredNodes = ghost.getJobs().getAskedNodesByDimension().get(dimension);

        while (!answeredNodes.contains(rndUnaskedNode.get())) { // wait for response
            try {
                logger.info("Waiting for answer from {}", rndUnaskedNode.get());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Queue<QueensJob> stolenJobsQueue = ghost.getJobs().getStolenJobsByDimension().get(dimension);
//        List<QueensJob> stolenJobs = new ArrayList<>();
//
//        while (!stolenJobsQueue.isEmpty()) {
//            stolenJobs.add(stolenJobsQueue.poll());
//        }
        return Optional.of(stolenJobsQueue);
    }

    @Async
    public void sendStolenJobs(Ghost ghost, int node, int dimension) {
        List<QueensJob> stolenJobs = queensService.pollHalfJobs(ghost, dimension);
        StolenJobsMessage stolenJobsMessage = new StolenJobsMessage(ghost.getConfiguration().getId(), node, dimension, stolenJobs);
        routingService.dispatchMessage(ghost, stolenJobsMessage, Methods.JOB_STEALING_COLLECTOR);
    }

}

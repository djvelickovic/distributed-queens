package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
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

    private Map<Integer, Set<Integer>> askedNodesByDimension = new ConcurrentHashMap<>();

//    private Map<Integer, Set<Integer>> asnweredNodesByDimension = new ConcurrentHashMap<>();

    private Map<Integer, Queue<QueensJob>> stolenJobsByDimension = new ConcurrentHashMap<>();

    @Autowired
    private RoutingService routingService;

    @Autowired
    private QueensService queensService;

    public void addStolenJobs(int from, int dimension, List<QueensJob> stolenJobs) {
        logger.info("Collected Stolen Jobs from {}, for dim {}. Jobs {}", from, dimension, stolenJobs.toString());
        askedNodesByDimension.get(dimension).add(from);
        stolenJobsByDimension.get(dimension).addAll(stolenJobs);
    }


    public Optional<Integer> getRandomUnaskedNode(int dimension) {

        Random rnd = new Random();

        Set<Integer> askedNodes = askedNodesByDimension.get(dimension);
        askedNodes.add(Configuration.id);

        while (true) {
            int genId = rnd.nextInt(Network.maxNodeInSystem) + 1;
            if (!askedNodes.contains(genId)) {
                return Optional.of(genId);
            }
            if (askedNodes.size() >= Network.maxNodeInSystem) {
                return Optional.empty();
            }
        }
    }

    public Optional<Queue<QueensJob>> stealJobs(int dimension) {
        askedNodesByDimension.putIfAbsent(dimension, ConcurrentHashMap.newKeySet());
        stolenJobsByDimension.putIfAbsent(dimension, new ConcurrentLinkedQueue<>());
//        asnweredNodesByDimension.putIfAbsent(dimension, ConcurrentHashMap.newKeySet());

        Optional<Integer> rndUnaskedNode = getRandomUnaskedNode(dimension);

        if (!rndUnaskedNode.isPresent()) {
            logger.info("All nodes are asked for jobs for dimension {}", dimension);
            return Optional.empty();
        }

        logger.info("Stealing from {} for dimension {}", rndUnaskedNode.get(), dimension);

        JobStealingMessage jobStealingMessage = new JobStealingMessage(Configuration.id, rndUnaskedNode.get(), dimension);

        routingService.dispatchMessage(jobStealingMessage, Methods.JOB_STEALING_REQUEST);

        Set<Integer> answeredNodes = askedNodesByDimension.get(dimension);

        while (!answeredNodes.contains(rndUnaskedNode.get())) { // wait for response
            try {
                logger.info("Waiting for answer from {}", rndUnaskedNode.get());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Queue<QueensJob> stolenJobsQueue = stolenJobsByDimension.get(dimension);
//        List<QueensJob> stolenJobs = new ArrayList<>();
//
//        while (!stolenJobsQueue.isEmpty()) {
//            stolenJobs.add(stolenJobsQueue.poll());
//        }
        return Optional.of(stolenJobsQueue);
    }

    @Async
    public void sendStolenJobs(int node, int dimension) {
        List<QueensJob> stolenJobs = queensService.pollHalfJobs(dimension);
        StolenJobsMessage stolenJobsMessage = new StolenJobsMessage(Configuration.id, node, dimension, stolenJobs);
        routingService.dispatchMessage(stolenJobsMessage, Methods.JOB_STEALING_COLLECTOR);
    }

}

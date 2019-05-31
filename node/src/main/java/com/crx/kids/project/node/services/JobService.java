package com.crx.kids.project.node.services;

import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.entities.QueensResult;
import com.crx.kids.project.node.messages.JobState;
import com.crx.kids.project.node.messages.StatusMessage;
import com.crx.kids.project.node.messages.StatusRequestMessage;
import com.crx.kids.project.node.common.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    public static final Map<String, Map<Integer, List<JobState>>> jobStatesByRequestId = new ConcurrentHashMap<>();

    public static final Map<Integer, Map<Integer, Integer[]>> collectedResultsByNodes = new ConcurrentHashMap<>();


    @Autowired
    private RoutingService routingService;

    @Autowired
    private QueensService queensService;


    public boolean stop() {
        return false;
    }


    public Result start(int dimension) {

        queensService.calculateJobsByDimension(dimension);
        queensService.startWorkForDimension(dimension);

        return Result.of(null);
    }

    public Result<QueensResult> result(int dimension) {
        return Result.of(new QueensResult());
    }


    public boolean pause() {
        QueensService.currentActiveDim = -1;
        return true;
    }

    public void putStatusMessage(StatusMessage statusMessage) {
//        Map<Integer, List<JobState>> jobStatesByNode = new ConcurrentHashMap<>();
//
//        if (jobStatesByRequestId.putIfAbsent(statusMessage.getStatusRequestId(), jobStatesByNode) != null) {
//            jobStatesByNode = jobStatesByRequestId.get(statusMessage.getStatusRequestId());
//        }

        Map<Integer, List<JobState>> jobStatesByNode = jobStatesByRequestId.get(statusMessage.getStatusRequestId());
        jobStatesByNode.put(statusMessage.getSender(), statusMessage.getJobStates());

        logger.info("Added status message from {} with request id {}", statusMessage.getSender(), statusMessage.getStatusRequestId());
    }

    public Map<Integer, String> getStatus() {
        String statusRequestId = UUID.randomUUID().toString();

        Map<Integer, List<JobState>> jobsStatesByNodes = new ConcurrentHashMap<>();
        jobStatesByRequestId.put(statusRequestId, jobsStatesByNodes);

        int sentToNodes = Network.maxNodeInSystem;

        IntStream.rangeClosed(1, sentToNodes)
                .filter(nodeId -> nodeId != Configuration.id)
                .mapToObj(nodeId -> new StatusRequestMessage(Configuration.id, nodeId, statusRequestId))
                .forEach(message -> routingService.dispatchMessage(message, Network.QUEENS_STATUS));


        while (true) {
            if (jobsStatesByNodes.size() != sentToNodes - 1) { // -1 if filter is active!
                logger.info("Waiting for status messages with request id {}. Received {}", statusRequestId, jobsStatesByNodes.size());
            }
            else {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        // TODO: combine messages


        Map<Integer, String> result = new HashMap<>();


        Map<Integer, List<JobState>> jobStatesByDimension = new HashMap<>();

        jobsStatesByNodes.values().stream()
                .flatMap(Collection::stream)
                .forEach(jobState -> {
                    jobStatesByDimension.putIfAbsent(jobState.getDimension(), new ArrayList<>());
                    jobStatesByDimension.get(jobState.getDimension()).add(jobState);
                });


        jobStatesByDimension.forEach((dimension, jobList) -> {
            result.put(dimension, reduce(jobList));
        });

        return result;
    }


    private String reduce(List<JobState> jobStates) {

        int done = jobStates.stream().map(JobState::getDone).reduce(Integer::sum).orElse(-1);
        int pending = jobStates.stream().map(JobState::getPending).reduce(Integer::sum).orElse(1);

        float percent = ((float) (done) / (float) (done + pending)) * 100f;

        String status = jobStates.stream().map(JobState::getStatus).reduce((s1, s2) -> {
            if (s1.equals(s2)) {
                return s1;
            }
            return "fuzzy";
        }).orElse("no items for status");

        return status + " ( "+percent+"% )";
    }

}

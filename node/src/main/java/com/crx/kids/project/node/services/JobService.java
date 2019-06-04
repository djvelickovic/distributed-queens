package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.entities.QueensResult;
import com.crx.kids.project.node.messages.JobState;
import com.crx.kids.project.node.messages.QueensResultBroadcast;
import com.crx.kids.project.node.messages.StatusMessage;
import com.crx.kids.project.node.messages.StatusRequestMessage;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    public static final Map<String, Map<Integer, List<JobState>>> jobStatesByRequestId = new ConcurrentHashMap<>();




    @Autowired
    private RoutingService routingService;

    @Autowired
    private QueensService queensService;


    @Autowired
    private JobStealingService jobStealingService;




    public boolean stop() {
        return false;
    }

    @Async
    public void initiateJobForDimension(int dimension) {

        Jobs.collectedResultsByDimensions.putIfAbsent(dimension, new ConcurrentHashMap<>());

        queensService.calculateJobsByDimension(dimension);
        startWorkForDimension(dimension);

    }


    public List<JobState> getJobsStates() {
        return  Jobs.jobsByDimensions.entrySet().stream()
                .map(e -> {
                    Queue<QueensResult> resultQueue = Jobs.calculatedResultsByDimensions.get(e.getKey());
                    if (resultQueue != null) {
                        String status;
                        if (e.getKey() == Jobs.currentActiveDim.get()){
                            status = "active";
                        }
                        else if (e.getValue().size() == 0 && resultQueue.size() > 0){
                            status = "done";
                        }
                        else if (e.getValue().size() != resultQueue.size()) {
                            status = "paused";
                        }
                        else {
                            logger.error("Shouldn not get here!");
                            status = "fuzzy";
                        }

                        return new JobState(e.getKey(), status, resultQueue.size(), e.getValue().size());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    @Async
    // move to job
    public void startWorkForDimension(int dimension) {
        Queue<QueensResult> resultsByDimension = new ConcurrentLinkedQueue<>();

        if (Jobs.calculatedResultsByDimensions.putIfAbsent(dimension, resultsByDimension) != null) {
            logger.info("Work for dimension has been already started: {}", dimension);

            if (Jobs.finishedJobs.contains(dimension)) {
                logger.error("Work for dimension finished: {}", dimension);
            }
            else {
                logger.info("Resuming for dimension has been already started: {}", dimension);
                Jobs.currentActiveDim.set(dimension);
            }
            return;
        }

        Jobs.currentActiveDim.set(dimension);
        logger.info("Starting work for dimension {}", dimension);

        Jobs.jobsByDimensions.putIfAbsent(dimension, new ConcurrentLinkedQueue<>());
        Queue<QueensJob> jobs = Jobs.jobsByDimensions.get(dimension);

        Queue<QueensResult> results = Jobs.calculatedResultsByDimensions.get(dimension);

        logger.info("Started consuming regular queue for dimension {}", dimension);
        consumeQueue(dimension, jobs, results);

        results.forEach(qr -> {
            if (qr.getResults() != null) {
                logger.debug("---- "+qr.getQueensJob().getJobId());
                qr.getResults().forEach(result -> logger.debug(Arrays.toString(result)));
            }
        });


        while (true) {
            Optional<Queue<QueensJob>> stolenJobsOptional = jobStealingService.stealJobs(dimension);
            if (!stolenJobsOptional.isPresent()) {
                break;
            }

            Queue<QueensJob> stolenJobs = stolenJobsOptional.get();

            while (!stolenJobs.isEmpty()) {
                jobs.add(stolenJobs.poll());
            }

            logger.info("Starting consuming stolen jobs! for dimension {}", dimension);
            consumeQueue(dimension, jobs, results);
        }

        logger.info("Finished work for dimension {}", dimension);
        Jobs.finishedJobs.add(dimension);
        Jobs.currentActiveDim.set(-1);

        QueensResultBroadcast queensResultBroadcast = new QueensResultBroadcast(Configuration.id, dimension, new ArrayList<>(results));
        logger.info("Broadcasting results for dimension {}. Result count = {}", dimension, results.size());
        routingService.broadcastMessage(queensResultBroadcast, Methods.QUEENS_RESULT_BROADCAST);

        Jobs.collectedResultsByDimensions.putIfAbsent(dimension, new ConcurrentHashMap<>());
        Map<Integer, List<Integer[]>> transformedResults = Jobs.collectedResultsByDimensions.get(dimension);

        results.forEach(qr -> transformedResults.put(qr.getQueensJob().getJobId(), qr.getResults()));

    }

    public void consumeQueue(int dimension, Queue<QueensJob> jobsQueue, Queue<QueensResult> resultQueue){
//        if (jobsQueue == null) {
//            logger.warn("Queue is null. Nothing to consme {}", dimension);
//            return;
//        }
        Random rnd = new Random();
        while (!jobsQueue.isEmpty()) {

            if (Jobs.currentActiveDim.get() != dimension) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Waiting calculations for dimension {}", dimension);
                continue;
            }

            QueensJob job = jobsQueue.poll();
            QueensResult qr = queensService.doQueensJob(job);
            resultQueue.add(qr);
            ThreadUtil.sleep(rnd.nextInt(80)+20);
            logger.info("Dim {}. Job {}. Finished",dimension, job.getJobId());
        }
    }


    public void addBroadcastFinishedResults(Integer dimension, List<QueensResult> queensResults) {
        Jobs.collectedResultsByDimensions.putIfAbsent(dimension, new ConcurrentHashMap<>());
        Map<Integer, List<Integer[]>> results = Jobs.collectedResultsByDimensions.get(dimension);

        queensResults.forEach(qr -> {
            results.put(qr.getQueensJob().getJobId(), qr.getResults());
        });
    }


    public Optional<List<Integer[]>> result(int dimension) {

        int maxJobsByDimension = queensService.getMaxJobNumberForDimension(dimension);
        if (Jobs.collectedResultsByDimensions.get(dimension) != null) {
            if (Jobs.collectedResultsByDimensions.get(dimension).size() == maxJobsByDimension) {
                return Optional.of(Jobs.collectedResultsByDimensions.get(dimension).values().stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            }
            else {
                logger.warn("Job for dimenison {} has not been finished. Required: {}, Curremt: {}", dimension, maxJobsByDimension, Jobs.collectedResultsByDimensions.get(dimension).size());
            }
        }
        else {
            logger.warn("Collected results for dimension {} are null", dimension);
        }

        return Optional.empty();
    }


    public boolean pause() {
        logger.info("Paused all job!");
        Jobs.currentActiveDim.set(-1);
        //
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
                .forEach(message -> routingService.dispatchMessage(message, Methods.QUEENS_STATUS));


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

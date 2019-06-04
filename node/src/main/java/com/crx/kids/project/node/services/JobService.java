package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Ghost;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.entities.QueensResult;
import com.crx.kids.project.node.messages.*;
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
    public void initiateJobForDimension(Ghost ghost, int dimension) {

        ghost.getJobs().getCollectedResultsByDimensions().putIfAbsent(dimension, new ConcurrentHashMap<>());

        queensService.calculateJobsByDimension(ghost, dimension);
        startWorkForDimension(ghost, dimension);
    }


    public List<JobState> getJobsStates(Ghost ghost) {
        return  ghost.getJobs().getJobsByDimensions().entrySet().stream()
                .map(e -> {
                    Queue<QueensResult> resultQueue = ghost.getJobs().getCalculatedResultsByDimensions().get(e.getKey());
                    if (resultQueue != null) {
                        String status;
                        if (e.getKey() == ghost.getJobs().getCurrentActiveDimension().get()){
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
    public void startWorkForDimension(Ghost ghost, int dimension) {
        Queue<QueensResult> resultsByDimension = new ConcurrentLinkedQueue<>();

        Jobs ghostJobs = ghost.getJobs();

        if (ghost.getJobs().getCalculatedResultsByDimensions().putIfAbsent(dimension, resultsByDimension) != null) {
            logger.info("Work for dimension has been already started: {}", dimension);

            if (ghost.getJobs().getFinishedJobs().contains(dimension)) {
                logger.error("Work for dimension finished: {}", dimension);
            }
            else {
                logger.info("Resuming for dimension has been already started: {}", dimension);
                ghost.getJobs().getCurrentActiveDimension().set(dimension);
            }
            return;
        }

        ghostJobs.getCurrentActiveDimension().set(dimension);
        logger.info("Starting work for dimension {}", dimension);

        ghostJobs.getJobsByDimensions().putIfAbsent(dimension, new ConcurrentLinkedQueue<>());
        Queue<QueensJob> jobs = ghostJobs.getJobsByDimensions().get(dimension);

        Queue<QueensResult> results = ghostJobs.getCalculatedResultsByDimensions().get(dimension);

        logger.info("Started consuming regular queue for dimension {}", dimension);
        consumeQueue(ghost, dimension, jobs, results);

        results.forEach(qr -> {
            if (qr.getResults() != null) {
                logger.debug("---- "+qr.getQueensJob().getJobId());
                qr.getResults().forEach(result -> logger.debug(Arrays.toString(result)));
            }
        });


        while (true) {
            Optional<Queue<QueensJob>> stolenJobsOptional = jobStealingService.stealJobs(ghost, dimension);
            if (!stolenJobsOptional.isPresent()) {
                break;
            }

            Queue<QueensJob> stolenJobs = stolenJobsOptional.get();

            while (!stolenJobs.isEmpty()) {
                jobs.add(stolenJobs.poll());
            }

            logger.info("Starting consuming stolen jobs! for dimension {}", dimension);
            consumeQueue(ghost, dimension, jobs, results);
        }

        logger.info("Finished work for dimension {}", dimension);
        ghostJobs.getFinishedJobs().add(dimension);
        ghostJobs.getCurrentActiveDimension().set(-1);

//        QueensResultBroadcast queensResultBroadcast = new QueensResultBroadcast(Configuration.id, dimension, new ArrayList<>(results));
//        logger.info("Broadcasting results for dimension {}. Result count = {}", dimension, results.size());
//        routingService.broadcastMessage(queensResultBroadcast, Methods.QUEENS_RESULT_BROADCAST);

        broadcastCalculatedResults(ghost, dimension);

        ghostJobs.getCollectedResultsByDimensions().putIfAbsent(dimension, new ConcurrentHashMap<>());
        Map<Integer, List<Integer[]>> transformedResults = ghostJobs.getCollectedResultsByDimensions().get(dimension);

        results.forEach(qr -> transformedResults.put(qr.getQueensJob().getJobId(), qr.getResults()));
    }

//    public void broadcastCalculatedResultsForAllUnfinishedJobs() {
//        Jobs.calculatedResultsByDimensions.forEach((dim, results) -> {
//            if (!Jobs.finishedJobs.contains(dim)) {
//                // it means that job is paused
//                broadcastCalculatedResults(dim);
//            }
//        });
//    }

    public void broadcastCalculatedResults(Ghost ghost, int dimension) {
        Queue<QueensResult> results = ghost.getJobs().getCalculatedResultsByDimensions().get(dimension);
        QueensResultBroadcast queensResultBroadcast = new QueensResultBroadcast(ghost.getConfiguration().getId(), dimension, new ArrayList<>(results));
        logger.info("Broadcasting results for dimension {}. Result count = {}", dimension, results.size());
        routingService.broadcastMessage(ghost, queensResultBroadcast, Methods.QUEENS_RESULT_BROADCAST);
    }


    public void consumeQueue(Ghost ghost, int dimension, Queue<QueensJob> jobsQueue, Queue<QueensResult> resultQueue){
//        if (jobsQueue == null) {
//            logger.warn("Queue is null. Nothing to consme {}", dimension);
//            return;
//        }
        Random rnd = new Random();
        while (!jobsQueue.isEmpty()) {

            if (ghost.getJobs().getCurrentActiveDimension().get() != dimension) {
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


    public void addBroadcastFinishedResults(Ghost ghost, Integer dimension, List<QueensResult> queensResults) {
        ghost.getJobs().getCollectedResultsByDimensions().putIfAbsent(dimension, new ConcurrentHashMap<>());
        Map<Integer, List<Integer[]>> results = ghost.getJobs().getCollectedResultsByDimensions().get(dimension);

        queensResults.forEach(qr -> {
            results.put(qr.getQueensJob().getJobId(), qr.getResults());
        });
    }


    public Optional<List<Integer[]>> result(Ghost ghost, int dimension) {

        Jobs ghostJobs = ghost.getJobs();

        int maxJobsByDimension = queensService.getMaxJobNumberForDimension(ghost, dimension);
        if (ghostJobs.getCollectedResultsByDimensions().get(dimension) != null) {
            if (ghostJobs.getCollectedResultsByDimensions().get(dimension).size() == maxJobsByDimension) {
                return Optional.of(ghostJobs.getCollectedResultsByDimensions().get(dimension).values().stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            }
            else {
                logger.warn("Job for dimenison {} has not been finished. Required: {}, Curremt: {}", dimension, maxJobsByDimension, ghostJobs.getCollectedResultsByDimensions().get(dimension).size());
            }
        }
        else {
            logger.warn("Collected results for dimension {} are null", dimension);
        }

        return Optional.empty();
    }


    public boolean pause(Ghost ghost) {
        logger.info("Paused all job!");
        ghost.getJobs().getCurrentActiveDimension().set(-1);
        BroadcastMessage<String> pauseBroadcastMessage = new BroadcastMessage<>(ghost.getConfiguration().getId(), UUID.randomUUID().toString());
        routingService.broadcastMessage(ghost, pauseBroadcastMessage, Methods.QUEENS_PAUSE);
        return true;
    }


    public void putStatusMessage(Ghost ghost, StatusMessage statusMessage) {
//        Map<Integer, List<JobState>> jobStatesByNode = new ConcurrentHashMap<>();
//
//        if (jobStatesByRequestId.putIfAbsent(statusMessage.getStatusRequestId(), jobStatesByNode) != null) {
//            jobStatesByNode = jobStatesByRequestId.get(statusMessage.getStatusRequestId());
//        }

        Map<Integer, List<JobState>> jobStatesByNode = ghost.getJobs().getJobStatesByRequestId().get(statusMessage.getStatusRequestId());
        jobStatesByNode.put(statusMessage.getSender(), statusMessage.getJobStates());

        logger.info("Added status message from {} with request id {}", statusMessage.getSender(), statusMessage.getStatusRequestId());
    }

    public Map<Integer, String> getStatus(Ghost ghost) {
        String statusRequestId = UUID.randomUUID().toString();

        Map<Integer, List<JobState>> jobsStatesByNodes = new ConcurrentHashMap<>();
        ghost.getJobs().getJobStatesByRequestId().put(statusRequestId, jobsStatesByNodes);

        int sentToNodes = ghost.getNetwork().getMaxNodeInSystem().get();

        IntStream.rangeClosed(1, sentToNodes)
                .filter(nodeId -> nodeId != ghost.getConfiguration().getId())
                .mapToObj(nodeId -> new StatusRequestMessage(ghost.getConfiguration().getId(), nodeId, statusRequestId))
                .forEach(message -> routingService.dispatchMessage(ghost, message, Methods.QUEENS_STATUS));


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

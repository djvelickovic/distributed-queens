package com.crx.kids.project.node.services;

import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.entities.QueensResult;
import com.crx.kids.project.node.messages.QueensJobsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class QueensService {

    private static final Logger logger = LoggerFactory.getLogger(QueensService.class);

    @Autowired
    private RoutingService routingService;


    public void addJobsForDimension(int dimension, List<QueensJob> jobs) {
        Queue<QueensJob> jobQueue = new ConcurrentLinkedQueue<>();

        if (Jobs.jobsByDimensions.putIfAbsent(dimension, jobQueue) != null) {
            jobQueue = Jobs.jobsByDimensions.get(dimension);
        }

        jobQueue.addAll(jobs);
    }



    public List<QueensJob> pollHalfJobs(int dimension) {

        Queue<QueensJob> jobQueue = Jobs.jobsByDimensions.get(dimension);

        if (jobQueue == null) {
            logger.error("There are no jobs for dimension {}", dimension);
            return new ArrayList<>();
        }

        List<QueensJob> queensJobs = new ArrayList<>();

        while (queensJobs.size() < jobQueue.size()) {
            queensJobs.add(jobQueue.poll());
        }

        logger.info("Polled {} jobs for dimension {}. Remained {}", queensJobs.size(), dimension, jobQueue.size());

        return queensJobs;
    }


    private List<QueensJob> pollJobs(int dimension, int maxJobs) {
        if (maxJobs <= 0) {
            return new ArrayList<>();
        }

        Queue<QueensJob> jobQueue = Jobs.jobsByDimensions.get(dimension);

        if (jobQueue == null) {
            logger.error("There are no jobs for dimension {}", dimension);
            return new ArrayList<>();
        }

        List<QueensJob> queensJobs = new ArrayList<>();

        for (int i = 0; i < maxJobs; i++) {
            QueensJob queensJob = jobQueue.poll();
            if (queensJob == null) {
                break;
            }
            queensJobs.add(queensJob);
        }

        logger.info("Polled {} jobs for dimension {}. Remained {}", queensJobs.size(), dimension, jobQueue.size());

        return queensJobs;
    }

    public void calculateJobsByDimension(int dimension) {
        Queue<QueensJob> queensJobs =  new ConcurrentLinkedQueue<>();

        if (Jobs.jobsByDimensions.putIfAbsent(dimension, queensJobs) != null) {
            Result schedResult = scheduleJobs(dimension, 0);
            if (schedResult.isError()) {
                logger.error("Error scheduling results for zero {}", schedResult.getError());
            }
            logger.warn("Jobs are already calculated for dim {}", dimension);
            return;
        }

        int jobs = getMaxJobNumberForDimension(dimension);

        List<QueensJob> jobList = IntStream.range(0, jobs)
                .mapToObj(i -> new QueensJob(dimension, jobs, i))
                .collect(Collectors.toList());

        queensJobs.addAll(jobList);

        logger.info("Calculated {} jobs for dimension {}", jobs, dimension);

        // send broadcast, and wait for nodes to request their parts, after all nodes are responded, send them their parts.
        // TODO: how do we know if all nodes responded?

        Result schedResult = scheduleJobs(dimension, jobs);

        if (schedResult.isError()) {
            logger.error("Error scheduling results {}", schedResult.getError());
        }
    }

    private Result scheduleJobs(int dimension, int maxJobs) {

        int maxNodeInSystem = Network.maxNodeInSystem;
        int jobsPerNode = maxJobs / maxNodeInSystem;

        IntStream.rangeClosed(1, maxNodeInSystem)
                .filter(nodeId -> nodeId != Configuration.id)
                .mapToObj(i -> {
                    List<QueensJob> queensJobs = pollJobs(dimension, jobsPerNode);
                    return new QueensJobsMessage(Configuration.id, i, dimension, queensJobs);
                })
                .forEach(m -> routingService.dispatchMessage(m, Methods.QUEENS_JOBS));

        return Result.of(null);
    }

    private int getMaxJobNumberForDimension(int dimension) {
        int absMaxJobs = (int) (100 / Configuration.limit);
        int jobs = 1;
        int cnt = 0;
        while ((absMaxJobs > jobs * dimension) && dimension > cnt) {
            jobs *= dimension;
            cnt++;
        }
        return jobs;
    }

    private Optional<Integer[]> calculateStartingQueens(QueensJob queensJob) {
        Integer[] startingQueens = new Integer[queensJob.getDimension()];

        for (int i = 0; i < startingQueens.length; i++) {
            startingQueens[i] = 0;
        }

        int jobId = queensJob.getJobId();
//        int position = 0;

        for (int position = 0; position < queensJob.columnsUsed(); position++) {
            int queen = jobId % queensJob.getDimension();

            if (attack(startingQueens, queen, position)) {
                return Optional.empty();
            }

            startingQueens[position] = queen;
            jobId /= queensJob.getDimension();
        }

        return Optional.of(startingQueens);
    }

    public QueensResult doQueensJob(QueensJob queensJob) {
        // columns
        Optional<Integer[]> queensOptional = calculateStartingQueens(queensJob);

        if (!queensOptional.isPresent()) {
            return new QueensResult(queensJob, new ArrayList<>());
        }

        List<Integer[]> results = subStepCalculation(queensOptional.get(), queensJob.columnsUsed(), queensJob.getDimension());
        return new QueensResult(queensJob, results);
    }

    private List<Integer[]> subStepCalculation(Integer[] queens, int columnForCheck, int dimension) {
        if (columnForCheck >= dimension) {
            return new ArrayList<>();
        }

        List<Integer[]> results = new ArrayList<>();

        for (int i = 0; i < dimension; i++) {
            queens[columnForCheck] = i;

            if (!attack(queens, i, columnForCheck)) {

                List<Integer[]> subResults = subStepCalculation(queens, columnForCheck + 1, dimension);
                results.addAll(subResults);

                if (columnForCheck == dimension - 1) {
                    results.add(Arrays.copyOf(queens, queens.length));
                }
            }
        }
        return results;
    }

    //    _ _ Q _ _ _
//    Q _ _ _ _ _
//    _ _ _ _ _ _
//    _ _ _ _ Q?_
//    _ Q _ _ _ _
//    _ _ _ Q _ _ =
    private boolean attack(Integer[] queens, int queen, int queenPosition) {
        if (queenPosition == 0) {
            return false;
        }

        for (int q = 0; q < queenPosition; q++) {

            if (queens[q] == queen) {
                return true;
            }
            int distance = queenPosition - q;

            int upperAttackField = queen + distance;
            int lowerAttackField = queen - distance;

            if (upperAttackField < queens.length) {
                if (upperAttackField == queens[q]){
                    return true;
                }
            }

            if (lowerAttackField >= 0) {
                if (lowerAttackField == queens[q]){
                    return true;
                }
            }
        }
        return false;
    }
}

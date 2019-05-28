package com.crx.kids.project.node.logic;

import com.crx.kids.project.common.util.Error;
import com.crx.kids.project.common.util.ErrorCode;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class QueensService {

    private static final Logger logger = LoggerFactory.getLogger(QueensService.class);

    private static final Map<Integer, Queue<QueensJob>> jobsByDimensions = new ConcurrentHashMap<>();

    private static final Map<Integer, Map<Integer, QueensResult>> collectedResultsByDimensions = new ConcurrentHashMap<>();

    private static final Set<Integer> finishedJobs = ConcurrentHashMap.newKeySet();

    public static int currentActiveDim;

    public static void main(String[] args) {
        QueensService queensService = new QueensService();
//        queensService.calculateResults(6);
//        int jobs = queensService.getMaxJobNumberForDimension(5);
//        logger.info("Max jobs "+jobs);

//        System.out.println(Arrays.toString(queensService.calculateStartingQueens(new QueensJob(5, 125, 7))));

//        queensService.doQueensJob(new QueensJob(5, 125, 7));


        queensService.calculateJobsByDimension(15);
        queensService.startWorkForDimension(15);


    }

    @Async
    public void startWorkForDimension(int dimension) {
        Map<Integer, QueensResult> resultsByDimension = new ConcurrentHashMap<>();

        if (collectedResultsByDimensions.putIfAbsent(dimension, resultsByDimension) != null) {
            logger.info("Work for dimension has been already started: {}", dimension);

            if (finishedJobs.contains(dimension)) {
                logger.error("Work for dimension finished: {}", dimension);
            }
            else {
                logger.info("Resuming for dimension has been already started: {}", dimension);
                currentActiveDim = dimension;
            }
            return;
        }

        currentActiveDim = dimension;
        logger.info("Starting work for dimension {}", dimension);

        Queue<QueensJob> jobs = jobsByDimensions.get(dimension);

        List<QueensResult> results = new ArrayList<>();

        while (!jobs.isEmpty()) {
            QueensJob job = jobs.poll();
            QueensResult qr = doQueensJob(job);
            results.add(qr);
        }

        results.forEach(qr -> {
            if (qr.getResults() != null) {
                logger.debug("---- "+qr.getQueensJob().getJobId());
                qr.getResults().forEach(result -> logger.debug(Arrays.toString(result)));
            }

            resultsByDimension.putIfAbsent(dimension, qr);
        });

        logger.info("Finished work for dimension {}", dimension);
        finishedJobs.add(dimension);
        currentActiveDim = -1;
    }

    public List<QueensJob> pollJobs(int dimension, int maxJobs) {
        return null;
    }

    public Result calculateJobsByDimension(int dimension) {
        Queue<QueensJob> queensJobs =  new ConcurrentLinkedQueue<>();

        if (jobsByDimensions.putIfAbsent(dimension, queensJobs) != null) {
            return Result.error(Error.of(ErrorCode.JOB_ERROR, "Jobs are already calculated for dim "+dimension));
        }

        int jobs = getMaxJobNumberForDimension(dimension);

        List<QueensJob> jobList = IntStream.range(0, jobs)
                .mapToObj(i -> new QueensJob(dimension, jobs, i))
                .collect(Collectors.toList());

        queensJobs.addAll(jobList);

        logger.info("Calculated {} jobs for dimension {}", jobs, dimension);

        // send broadcast, and wait for nodes to request their parts, after all nodes are responded, send them their parts.
        // TODO: how do we know if all nodes responded?

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

    private QueensResult doQueensJob(QueensJob queensJob) {
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

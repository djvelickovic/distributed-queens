package com.crx.kids.project.node.common;

import com.crx.kids.project.node.common.dto.JobsDTO;
import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.entities.QueensResult;
import com.crx.kids.project.node.messages.JobState;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Jobs {

    private Map<Integer, Queue<QueensJob>> jobsByDimensions;
    private Map<Integer, Queue<QueensResult>> calculatedResultsByDimensions;
    private Set<Integer> finishedJobs;
    private Map<Integer, Map<Integer, List<Integer[]>>> collectedResultsByDimensions;
    private AtomicInteger currentActiveDimension;

    // job stealing maps
    private Map<Integer, Set<Integer>> askedNodesByDimension;
    private Map<Integer, Queue<QueensJob>> stolenJobsByDimension;

    private Map<String, Map<Integer, List<JobState>>> jobStatesByRequestId;


    public Jobs(JobsDTO jobsDTO) {

    }

    public Jobs() {

    }

    public Map<String, Map<Integer, List<JobState>>> getJobStatesByRequestId() {
        return jobStatesByRequestId;
    }

    public void setJobStatesByRequestId(Map<String, Map<Integer, List<JobState>>> jobStatesByRequestId) {
        this.jobStatesByRequestId = jobStatesByRequestId;
    }

    public Map<Integer, Set<Integer>> getAskedNodesByDimension() {
        return askedNodesByDimension;
    }

    public void setAskedNodesByDimension(Map<Integer, Set<Integer>> askedNodesByDimension) {
        this.askedNodesByDimension = askedNodesByDimension;
    }

    public Map<Integer, Queue<QueensJob>> getStolenJobsByDimension() {
        return stolenJobsByDimension;
    }

    public void setStolenJobsByDimension(Map<Integer, Queue<QueensJob>> stolenJobsByDimension) {
        this.stolenJobsByDimension = stolenJobsByDimension;
    }

    public Map<Integer, Queue<QueensJob>> getJobsByDimensions() {
        return jobsByDimensions;
    }

    public void setJobsByDimensions(Map<Integer, Queue<QueensJob>> jobsByDimensions) {
        this.jobsByDimensions = jobsByDimensions;
    }

    public Map<Integer, Queue<QueensResult>> getCalculatedResultsByDimensions() {
        return calculatedResultsByDimensions;
    }

    public void setCalculatedResultsByDimensions(Map<Integer, Queue<QueensResult>> calculatedResultsByDimensions) {
        this.calculatedResultsByDimensions = calculatedResultsByDimensions;
    }

    public Set<Integer> getFinishedJobs() {
        return finishedJobs;
    }

    public void setFinishedJobs(Set<Integer> finishedJobs) {
        this.finishedJobs = finishedJobs;
    }

    public Map<Integer, Map<Integer, List<Integer[]>>> getCollectedResultsByDimensions() {
        return collectedResultsByDimensions;
    }

    public void setCollectedResultsByDimensions(Map<Integer, Map<Integer, List<Integer[]>>> collectedResultsByDimensions) {
        this.collectedResultsByDimensions = collectedResultsByDimensions;
    }

    public AtomicInteger getCurrentActiveDimension() {
        return currentActiveDimension;
    }

    public void setCurrentActiveDimension(AtomicInteger currentActiveDimension) {
        this.currentActiveDimension = currentActiveDimension;
    }
}

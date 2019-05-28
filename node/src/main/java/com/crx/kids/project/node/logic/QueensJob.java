package com.crx.kids.project.node.logic;

public class QueensJob {
    private int dimension;
    private int maxJobs;
    private int jobId;

    public QueensJob() {
    }

    public QueensJob(int dimension, int maxJobs, int jobId) {
        this.dimension = dimension;
        this.maxJobs = maxJobs;
        this.jobId = jobId;
    }

    public int columnsUsed() {
        int cnt = 0;
        int jobs = maxJobs;
        while ((jobs = jobs / dimension) != 0) {
            cnt++;
        }

        return cnt;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getMaxJobs() {
        return maxJobs;
    }

    public void setMaxJobs(int maxJobs) {
        this.maxJobs = maxJobs;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}

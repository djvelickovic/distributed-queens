package com.crx.kids.project.node.messages;

import com.crx.kids.project.node.entities.QueensJob;

import java.util.List;

public class QueensJobsMessage extends Message {
    private List<QueensJob> jobs;
    private int dimension;

    public QueensJobsMessage() {
    }

    public QueensJobsMessage(int sender, int receiver, int dimension, List<QueensJob> jobs) {
        super(sender, receiver);
        this.jobs = jobs;
        this.dimension = dimension;
    }

    public List<QueensJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<QueensJob> jobs) {
        this.jobs = jobs;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "QueensJobsMessage{" +
                "jobs=" + jobs +
                ", dimension=" + dimension +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

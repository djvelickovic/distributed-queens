package com.crx.kids.project.node.messages;

import com.crx.kids.project.node.entities.QueensJob;

import java.util.List;

public class StolenJobsMessage extends Message {

    private int dimension;
    private List<QueensJob> stolenJobs;

    public StolenJobsMessage() {
    }

    public StolenJobsMessage(int sender, int receiver, int dimension, List<QueensJob> stolenJobs) {
        super(sender, receiver);
        this.dimension = dimension;
        this.stolenJobs = stolenJobs;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public List<QueensJob> getStolenJobs() {
        return stolenJobs;
    }

    public void setStolenJobs(List<QueensJob> stolenJobs) {
        this.stolenJobs = stolenJobs;
    }

    @Override
    public String toString() {
        return "StolenJobsMessage{" +
                "dimension=" + dimension +
                ", stolenJobs=" + stolenJobs +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

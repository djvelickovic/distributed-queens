package com.crx.kids.project.node.messages;

public class JobState {
    private int dimension;
    private String status;
    private int done;
    private int pending;

    public JobState(int dimension, String status, int done, int pending) {
        this.dimension = dimension;
        this.status = status;
        this.done = done;
        this.pending = pending;
    }


    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getDone() {
        return done;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    @Override
    public String toString() {
        return "JobState{" +
                "dimension=" + dimension +
                ", status='" + status + '\'' +
                ", done=" + done +
                ", pending=" + pending +
                '}';
    }
}

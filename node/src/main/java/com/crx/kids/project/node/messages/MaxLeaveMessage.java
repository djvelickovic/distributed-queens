package com.crx.kids.project.node.messages;

public class MaxLeaveMessage extends Message {
    private int activeJob;

    public MaxLeaveMessage() {
    }

    public MaxLeaveMessage(int sender, int receiver, int activeJob) {
        super(sender, receiver);
        this.activeJob = activeJob;
    }

    public int getActiveJob() {
        return activeJob;
    }

    public void setActiveJob(int activeJob) {
        this.activeJob = activeJob;
    }
}

package com.crx.kids.project.node.messages;

public class JobStealingMessage extends DirectMessage {

    private int dimension;

    public JobStealingMessage() {
    }

    public JobStealingMessage(int sender, int receiver, int dimension) {
        super(sender, receiver);
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "JobStealingMessage{" +
                "dimension=" + dimension +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

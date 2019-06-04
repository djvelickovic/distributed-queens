package com.crx.kids.project.node.messages;

public class DirectMessage extends Message {
    protected int receiver;


    public DirectMessage() {
    }

    public DirectMessage(int sender, int receiver) {
        super(sender);
        this.receiver = receiver;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "DirectMessage{" +
                "receiver=" + receiver +
                ", sender=" + sender +
                ", trace=" + trace +
                '}';
    }
}

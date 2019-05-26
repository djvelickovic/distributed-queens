package com.crx.kids.project.node.messages;

import java.util.ArrayList;
import java.util.List;

public abstract class Message {

    private int sender;
    private int receiver;
    private List<Trace> trace;

    public Message() {
    }

    public Message(int sender, int receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public List<Trace> getTrace() {
        return trace;
    }

    public void addTrace(Trace t) {
        if (trace == null) {
            trace = new ArrayList<>();
        }
        trace.add(t);
    }

    public void setTrace(List<Trace> trace) {
        this.trace = trace;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", trace=" + trace +
                '}';
    }
}

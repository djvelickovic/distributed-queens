package com.crx.kids.project.node.messages;

import java.util.ArrayList;
import java.util.List;

public abstract class Message {

    private Integer sender;
    private Integer receiver;
    private List<Integer> path = new ArrayList<>();

    public Message() {
    }

    public Message(Integer sender, Integer receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public Integer getSender() {
        return sender;
    }

    public void setSender(Integer sender) {
        this.sender = sender;
    }

    public Integer getReceiver() {
        return receiver;
    }

    public void setReceiver(Integer receiver) {
        this.receiver = receiver;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void addNodeToPath(Integer id) {
        path.add(id);
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", path=" + path +
                '}';
    }
}

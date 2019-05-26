package com.crx.kids.project.node.messages;

public class Trace {
    private int nodeId;
    private String log;

    public Trace() {
    }

    public Trace(int nodeId, String log) {
        this.nodeId = nodeId;
        this.log = log;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String toString() {
        return "Trace{" +
                "nodeId=" + nodeId +
                ", log='" + log + '\'' +
                '}';
    }
}

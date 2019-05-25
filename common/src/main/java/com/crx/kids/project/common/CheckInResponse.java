package com.crx.kids.project.common;

public class CheckInResponse {
    private int id;
    private NodeInfo nodeInfo;

    public CheckInResponse(int id, NodeInfo nodeInfo) {
        this.id = id;
        this.nodeInfo = nodeInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    @Override
    public String toString() {
        return "CheckInResponse{" +
                "id=" + id +
                ", nodeInfo=" + nodeInfo +
                '}';
    }
}

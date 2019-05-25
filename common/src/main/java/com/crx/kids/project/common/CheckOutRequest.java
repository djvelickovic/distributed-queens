package com.crx.kids.project.common;

public class CheckOutRequest {

    private int id;
    private NodeInfo nodeInfo;

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
        return "CheckOutRequest{" +
                "id=" + id +
                ", nodeInfo=" + nodeInfo +
                '}';
    }
}

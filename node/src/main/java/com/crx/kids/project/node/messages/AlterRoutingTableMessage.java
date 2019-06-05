package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;

public class AlterRoutingTableMessage extends Message { // not broadcast, nodeId is same as sender id
    private NodeInfo nodeInfo;
    private boolean delete;

    public AlterRoutingTableMessage() {
    }

    public AlterRoutingTableMessage(int sender, int receiver, boolean delete, NodeInfo nodeInfo) {
        super(sender, receiver);
        this.nodeInfo = nodeInfo;
        this.delete = delete;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
}

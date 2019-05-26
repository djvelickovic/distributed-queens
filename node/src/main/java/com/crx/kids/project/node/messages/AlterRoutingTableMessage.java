package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;

public class AlterRoutingTableMessage extends Message { // not broadcast, nodeId is same as sender id
    private boolean ghost;
    private NodeInfo nodeInfo;

    public AlterRoutingTableMessage() {
    }

    public AlterRoutingTableMessage(int sender, int receiver, boolean ghost, NodeInfo nodeInfo) {
        super(sender, receiver);
        this.ghost = ghost;
        this.nodeInfo = nodeInfo;
    }

    public boolean isGhost() {
        return ghost;
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
}

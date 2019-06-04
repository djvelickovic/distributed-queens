package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;

public class AlterRoutingTableMessage extends Message { // not broadcast, nodeId is same as sender id
    private NodeInfo nodeInfo;

    public AlterRoutingTableMessage() {
    }

    public AlterRoutingTableMessage(int sender, int receiver, NodeInfo nodeInfo) {
        super(sender, receiver);
        this.nodeInfo = nodeInfo;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
}

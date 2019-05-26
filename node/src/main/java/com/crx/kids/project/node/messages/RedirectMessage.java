package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;

public class RedirectMessage extends Message {
    private NodeInfo nodeInfo;

    public RedirectMessage() {
    }

    public RedirectMessage(int sender, int receiver, NodeInfo nodeInfo) {
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

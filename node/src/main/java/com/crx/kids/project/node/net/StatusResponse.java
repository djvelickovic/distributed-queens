package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.FullNodeInfo;

import java.util.Map;

public class StatusResponse {
    private Map<Integer, NodeInfo> nodes;
    private FullNodeInfo firstSmallest;
    private FullNodeInfo secondSmallest;

    public StatusResponse() {
    }

    public Map<Integer, NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(Map<Integer, NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public FullNodeInfo getFirstSmallest() {
        return firstSmallest;
    }

    public void setFirstSmallest(FullNodeInfo firstSmallest) {
        this.firstSmallest = firstSmallest;
    }

    public FullNodeInfo getSecondSmallest() {
        return secondSmallest;
    }

    public void setSecondSmallest(FullNodeInfo secondSmallest) {
        this.secondSmallest = secondSmallest;
    }
}

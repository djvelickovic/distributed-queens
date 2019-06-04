package com.crx.kids.project.node.endpoints.dto;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.FullNodeInfo;

import java.util.Map;

public class StatusResponse {
    private int myself;
    private Map<Integer, NodeInfo> nodes;
    private FullNodeInfo firstSmallest;
    private FullNodeInfo secondSmallest;
    private boolean tokenIdle;
    private boolean tokenHere;


    public StatusResponse() {
    }

    public boolean isTokenIdle() {
        return tokenIdle;
    }

    public void setTokenIdle(boolean tokenIdle) {
        this.tokenIdle = tokenIdle;
    }

    public boolean isTokenHere() {
        return tokenHere;
    }

    public void setTokenHere(boolean tokenHere) {
        this.tokenHere = tokenHere;
    }

    public int getMyself() {
        return myself;
    }

    public void setMyself(int myself) {
        this.myself = myself;
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

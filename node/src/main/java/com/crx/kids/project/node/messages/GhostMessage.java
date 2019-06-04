package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.entities.QueensJob;

import java.util.Map;
import java.util.Queue;

public class GhostMessage extends DirectMessage {
    private Map<Integer, NodeInfo> routingTable;

    private Map<Integer, Queue<QueensJob>> unfinishedJobsForDimension;
    private Integer stoppedJob;

    public GhostMessage() {
    }

    public GhostMessage(int sender, int receiver, Integer stoppedJob, Map<Integer, NodeInfo> routingTable, Map<Integer, Queue<QueensJob>> unfinishedJobsForDimension) {
        super(sender, receiver);
        this.routingTable = routingTable;
        this.unfinishedJobsForDimension = unfinishedJobsForDimension;
        this.stoppedJob = stoppedJob;
    }

    public Integer getStoppedJob() {
        return stoppedJob;
    }

    public void setStoppedJob(Integer stoppedJob) {
        this.stoppedJob = stoppedJob;
    }

    public Map<Integer, NodeInfo> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Integer, NodeInfo> routingTable) {
        this.routingTable = routingTable;
    }

    public Map<Integer, Queue<QueensJob>> getUnfinishedJobsForDimension() {
        return unfinishedJobsForDimension;
    }

    public void setUnfinishedJobsForDimension(Map<Integer, Queue<QueensJob>> unfinishedJobsForDimension) {
        this.unfinishedJobsForDimension = unfinishedJobsForDimension;
    }
}

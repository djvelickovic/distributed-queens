package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.entities.QueensJob;

import java.util.Map;
import java.util.Queue;

public class HostMessage extends Message {
    private Map<Integer, NodeInfo> routingTable;
    private Map<Integer, Queue<QueensJob>> unfinishedJobsForDimension;
    private Integer stoppedJob;
    private NodeInfo myself;

    public HostMessage() {
    }

    public HostMessage(int sender, int receiver, Integer stoppedJob, Map<Integer, NodeInfo> routingTable, Map<Integer, Queue<QueensJob>> unfinishedJobsForDimension, NodeInfo myself) {
        super(sender, receiver);
        this.routingTable = routingTable;
        this.unfinishedJobsForDimension = unfinishedJobsForDimension;
        this.stoppedJob = stoppedJob;
        this.myself = myself;
    }

    public NodeInfo getMyself() {
        return myself;
    }

    public void setMyself(NodeInfo myself) {
        this.myself = myself;
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

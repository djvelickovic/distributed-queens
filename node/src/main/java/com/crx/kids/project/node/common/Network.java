package com.crx.kids.project.node.common;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.common.dto.NetworkDTO;
import com.crx.kids.project.node.messages.FullNodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Network {

    public final ReentrantReadWriteLock configurationLock = new ReentrantReadWriteLock();
    public final ReentrantReadWriteLock maxNodeLock = new ReentrantReadWriteLock();

    private NodeInfo firstKnownNode;
    private AtomicInteger maxNodeInSystem;
    private Map<Integer, NodeInfo> routingTable;
    private FullNodeInfo firstSmallestNeighbour;
    private FullNodeInfo secondSmallestNeighbour;

    public Network() {

    }

    public Network(NetworkDTO networkDTO) {

    }

    public NodeInfo getFirstKnownNode() {
        return firstKnownNode;
    }

    public void setFirstKnownNode(NodeInfo firstKnownNode) {
        this.firstKnownNode = firstKnownNode;
    }

    public void setMaxNodeInSystem(AtomicInteger maxNodeInSystem) {
        this.maxNodeInSystem = maxNodeInSystem;
    }

    public AtomicInteger getMaxNodeInSystem() {
        return maxNodeInSystem;
    }

    public Map<Integer, NodeInfo> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Integer, NodeInfo> routingTable) {
        this.routingTable = routingTable;
    }

    public FullNodeInfo getFirstSmallestNeighbour() {
        return firstSmallestNeighbour;
    }

    public void setFirstSmallestNeighbour(FullNodeInfo firstSmallestNeighbour) {
        this.firstSmallestNeighbour = firstSmallestNeighbour;
    }

    public FullNodeInfo getSecondSmallestNeighbour() {
        return secondSmallestNeighbour;
    }

    public void setSecondSmallestNeighbour(FullNodeInfo secondSmallestNeighbour) {
        this.secondSmallestNeighbour = secondSmallestNeighbour;
    }

}

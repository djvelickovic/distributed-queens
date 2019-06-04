package com.crx.kids.project.node.common;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.FullNodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Network {


    public static NodeInfo firstKnownNode;
    public static final ReentrantReadWriteLock configurationLock = new ReentrantReadWriteLock();
    public static final ReentrantReadWriteLock maxNodeLock = new ReentrantReadWriteLock();

    public static final AtomicInteger maxNodeInSystem = new AtomicInteger(0);

    public static final Map<Integer, NodeInfo> neighbours = new ConcurrentHashMap<>();

//
//    public static FullNodeInfo firstSmallestNeighbour;
//    public static FullNodeInfo secondSmallestNeighbour;

    public static FullNodeInfo firstSmallestNeighbour() {
        return neighbours.entrySet().stream()
                .reduce((e1, e2) -> e1.getKey() > e2.getKey() ? e2 : e1)
                .map(e -> new FullNodeInfo(e.getKey(), e.getValue()))
                .orElse(null);
    }

    public static FullNodeInfo secondSmallestNeighbour() {
        FullNodeInfo firstSmallest = firstSmallestNeighbour();
        return neighbours.entrySet().stream()
                .filter(e -> e.getKey() != firstSmallest.getId())
                .reduce((e1, e2) -> e1.getKey() > e2.getKey() ? e2 : e1)
                .map(e -> new FullNodeInfo(e.getKey(), e.getValue()))
                .orElse(firstSmallest);
    }

}

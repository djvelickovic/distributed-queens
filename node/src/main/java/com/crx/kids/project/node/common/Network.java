package com.crx.kids.project.node.common;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.FullNodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Network {


    public static NodeInfo firstKnownNode;
    public static final ReentrantReadWriteLock configurationLock = new ReentrantReadWriteLock();
    public static final ReentrantReadWriteLock maxNodeLock = new ReentrantReadWriteLock();

    public static int maxNodeInSystem;

    public static final Map<Integer, NodeInfo> neighbours = new ConcurrentHashMap<>();

    public static FullNodeInfo firstSmallestNeighbour;
    public static FullNodeInfo secondSmallestNeighbour;




}

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

    public static String NEWBIE_JOIN = "node/net/newbie-join";
    public static String NEWBIE_ACCEPTED = "node/net/newbie-accepted";
    public static String ALTER_NEIGHBOURS = "node/net/alter-neighbours";
    public static String PING = "node/net/ping";

    public static final String STATUS = "node/net/status";

    public static String BROADCAST_JOIN = "node/net/join-broadcast";
    public static String BROADCAST_LEAVE = "node/net/leave-broadcast";
    public static String BROADCAST_CRITICAL_SECTION = "node/net/critical-section-broadcast";
    public static String CRITICAL_SECTION_TOKEN = "node/net/critical-section-token";

    public static final String QUEENS_JOBS = "node/net/queens";
    public static final String QUEENS_START = "node/net/queens-start";
    public static final String QUEENS_PAUSE = "node/net/queens-pause";
    public static final String QUEENS_STATUS = "node/net/queens-status";
    public static final String QUEENS_STATUS_COLLECTOR = "node/net/queens-status-collector";


}

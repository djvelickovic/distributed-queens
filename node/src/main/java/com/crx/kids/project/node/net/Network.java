package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.FullNodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Network {

    public static NodeInfo firstKnownNode;
    public static final ReentrantReadWriteLock configurationLock = new ReentrantReadWriteLock();

    public static final Map<Integer, NodeInfo> neighbours = new ConcurrentHashMap<>();
    public static FullNodeInfo firstSmallestNeighbour;
    public static FullNodeInfo secondSmallestNeighbour;

    public static String NEWBIE_JOIN = "node/net/newbie-join";
    public static String NEWBIE_ACCEPTED = "node/net/newbie-accepted";
    public static String ALTER_NEIGHBOURS = "node/net/alter-neighbours";
    public static String PING = "node/net/ping";


}

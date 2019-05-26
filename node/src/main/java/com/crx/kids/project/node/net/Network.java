package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Network {

    public static NodeInfo firstKnownNode;

    public static final Map<Integer, NodeInfo> neighbours = new ConcurrentHashMap<>();

}

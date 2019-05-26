package com.crx.kids.project.node.logic;

import com.crx.kids.project.common.NodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Network {

    public static final Map<Integer, NodeInfo> neighbours = new ConcurrentHashMap<>();

}

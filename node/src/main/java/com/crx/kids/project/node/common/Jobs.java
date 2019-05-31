package com.crx.kids.project.node.common;

import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.entities.QueensResult;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Jobs {

    // move to job common class
    public static final Map<Integer, Queue<QueensJob>> jobsByDimensions = new ConcurrentHashMap<>();
    public static final Map<Integer, Queue<QueensResult>> collectedResultsByDimensions = new ConcurrentHashMap<>();
    public static final Set<Integer> finishedJobs = ConcurrentHashMap.newKeySet();

    public static volatile int currentActiveDim;

}

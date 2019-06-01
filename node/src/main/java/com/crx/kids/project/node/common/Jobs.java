package com.crx.kids.project.node.common;

import com.crx.kids.project.node.entities.QueensJob;
import com.crx.kids.project.node.entities.QueensResult;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Jobs {

    // move to job common class
    public static final Map<Integer, Queue<QueensJob>> jobsByDimensions = new ConcurrentHashMap<>();
    public static final Map<Integer, Queue<QueensResult>> calculatedResultsByDimensions = new ConcurrentHashMap<>();
    public static final Set<Integer> finishedJobs = ConcurrentHashMap.newKeySet();

    public static final AtomicInteger currentActiveDim = new AtomicInteger(-1);

}

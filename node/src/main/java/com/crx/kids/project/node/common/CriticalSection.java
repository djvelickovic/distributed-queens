package com.crx.kids.project.node.common;

import com.crx.kids.project.node.entities.CriticalSectionToken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CriticalSection {

    public static final ReentrantReadWriteLock criticalSectionLock = new ReentrantReadWriteLock();
    public static final Map<Integer, Integer> suzukiKasamiCounterByNodes = new ConcurrentHashMap<>();
    public static final AtomicInteger suzukiKasamiCounter = new AtomicInteger(0);


    public static final AtomicBoolean tokenIdle = new AtomicBoolean(false);

    public static CriticalSectionToken token;
}

package com.crx.kids.project.node.common;

import com.crx.kids.project.node.common.dto.CriticalSectionDTO;
import com.crx.kids.project.node.entities.CriticalSectionToken;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CriticalSection {

    private ReentrantReadWriteLock criticalSectionLock = new ReentrantReadWriteLock();

    private Map<Integer, Integer> suzukiKasamiCounterByNodes = new ConcurrentHashMap<>();
    private AtomicInteger suzukiKasamiCounter = new AtomicInteger(0);
    private AtomicBoolean tokenIdle = new AtomicBoolean(false);

    private CriticalSectionToken token;


    public CriticalSection() {
    }

    public CriticalSection(CriticalSectionDTO criticalSectionDTO) {
    }


    public ReentrantReadWriteLock getCriticalSectionLock() {
        return criticalSectionLock;
    }

    public void setCriticalSectionLock(ReentrantReadWriteLock criticalSectionLock) {
        this.criticalSectionLock = criticalSectionLock;
    }

    public Map<Integer, Integer> getSuzukiKasamiCounterByNodes() {
        return suzukiKasamiCounterByNodes;
    }

    public void setSuzukiKasamiCounterByNodes(Map<Integer, Integer> suzukiKasamiCounterByNodes) {
        this.suzukiKasamiCounterByNodes = suzukiKasamiCounterByNodes;
    }

    public AtomicInteger getSuzukiKasamiCounter() {
        return suzukiKasamiCounter;
    }

    public void setSuzukiKasamiCounter(AtomicInteger suzukiKasamiCounter) {
        this.suzukiKasamiCounter = suzukiKasamiCounter;
    }

    public AtomicBoolean getTokenIdle() {
        return tokenIdle;
    }

    public void setTokenIdle(AtomicBoolean tokenIdle) {
        this.tokenIdle = tokenIdle;
    }

    public CriticalSectionToken getToken() {
        return token;
    }

    public void setToken(CriticalSectionToken token) {
        this.token = token;
    }

}

package com.crx.kids.project.node.cs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CriticalSectionToken {

    private Queue<Integer> queue;
    private Map<Integer, Integer> suzukiKasamiNodeMap;

    public CriticalSectionToken() {
    }

    public Queue<Integer> getQueue() {
        if (queue == null) {
            queue = new LinkedList<>();
        }
        return queue;
    }

    public void setQueue(Queue<Integer> queue) {
        this.queue = queue;
    }

    public Map<Integer, Integer> getSuzukiKasamiNodeMap() {
        if (suzukiKasamiNodeMap == null) {
            suzukiKasamiNodeMap = new HashMap<>();
        }
        return suzukiKasamiNodeMap;
    }

    public void setSuzukiKasamiNodeMap(Map<Integer, Integer> suzukiKasamiNodeMap) {
        this.suzukiKasamiNodeMap = suzukiKasamiNodeMap;
    }

    @Override
    public String toString() {
        return "CriticalSectionToken{" +
                "queue=" + queue +
                ", suzukiKasamiNodeMap=" + suzukiKasamiNodeMap +
                '}';
    }
}

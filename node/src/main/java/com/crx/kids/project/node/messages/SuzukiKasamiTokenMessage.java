package com.crx.kids.project.node.messages;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SuzukiKasamiTokenMessage extends Message {

    private Queue<Integer> queue;
    private Map<Integer, Integer> suzukiKasamiNodeMap;

    public SuzukiKasamiTokenMessage() {
        // do not use this constructor
    }

    public SuzukiKasamiTokenMessage(int sender, int receiver) {
        super(sender, receiver);
        queue = new LinkedList<>();
        suzukiKasamiNodeMap = new HashMap<>();
    }

    public Queue<Integer> getQueue() {
        return queue;
    }

    public void setQueue(Queue<Integer> queue) {
        this.queue = queue;
    }

    public Map<Integer, Integer> getSuzukiKasamiNodeMap() {
        return suzukiKasamiNodeMap;
    }

    public void setSuzukiKasamiNodeMap(Map<Integer, Integer> suzukiKasamiNodeMap) {
        this.suzukiKasamiNodeMap = suzukiKasamiNodeMap;
    }

    @Override
    public String toString() {
        return "SuzukiKasamiTokenMessage{" +
                "queue=" + queue +
                ", suzukiKasamiNodeMap=" + suzukiKasamiNodeMap +
                '}';
    }
}

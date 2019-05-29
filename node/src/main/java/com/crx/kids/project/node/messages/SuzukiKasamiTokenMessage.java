package com.crx.kids.project.node.messages;

import com.crx.kids.project.node.cs.CriticalSectionToken;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SuzukiKasamiTokenMessage extends Message {

    private CriticalSectionToken criticalSectionToken;

    public SuzukiKasamiTokenMessage() {
        // do not use this constructor
    }

    public SuzukiKasamiTokenMessage(int sender, int receiver, CriticalSectionToken token) {
        super(sender, receiver);
        this.criticalSectionToken = token;
    }

    public CriticalSectionToken getCriticalSectionToken() {
        return criticalSectionToken;
    }

    public void setCriticalSectionToken(CriticalSectionToken criticalSectionToken) {
        this.criticalSectionToken = criticalSectionToken;
    }

    @Override
    public String toString() {
        return "SuzukiKasamiTokenMessage{" +
                "criticalSectionToken=" + criticalSectionToken +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

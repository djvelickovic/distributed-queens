package com.crx.kids.project.node.messages;

import com.crx.kids.project.node.entities.CriticalSectionToken;

public class SuzukiKasamiTokenMessage extends DirectMessage {

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

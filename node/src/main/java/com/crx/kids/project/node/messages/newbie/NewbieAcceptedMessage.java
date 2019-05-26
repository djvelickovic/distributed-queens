package com.crx.kids.project.node.messages.newbie;

import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.Message;

public class NewbieAcceptedMessage extends Message {
    // first must be smaller than second by id
    private FullNodeInfo firstNeighbour;
    private FullNodeInfo secondNeighbour;

    public NewbieAcceptedMessage() {
    }

    public NewbieAcceptedMessage(int sender, int receiver, FullNodeInfo firstNeighbour, FullNodeInfo secondNeighbour) {
        super(sender, receiver);
        this.firstNeighbour = firstNeighbour;
        this.secondNeighbour = secondNeighbour;
    }

    public FullNodeInfo getFirstNeighbour() {
        return firstNeighbour;
    }

    public void setFirstNeighbour(FullNodeInfo firstNeighbour) {
        this.firstNeighbour = firstNeighbour;
    }

    public FullNodeInfo getSecondNeighbour() {
        return secondNeighbour;
    }

    public void setSecondNeighbour(FullNodeInfo secondNeighbour) {
        this.secondNeighbour = secondNeighbour;
    }
}

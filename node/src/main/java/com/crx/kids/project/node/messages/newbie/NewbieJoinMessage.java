package com.crx.kids.project.node.messages.newbie;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.Message;

public class NewbieJoinMessage extends Message {

    //sender id is newbie Id
    private NodeInfo newbie;

    public NewbieJoinMessage() {
    }

    public NewbieJoinMessage(int sender, int receiver, NodeInfo newbie) {
        super(sender, receiver);
        this.newbie = newbie;
    }

    public NodeInfo getNewbie() {
        return newbie;
    }

    public void setNewbie(NodeInfo newbie) {
        this.newbie = newbie;
    }
}

package com.crx.kids.project.node.messages.newbie;

import com.crx.kids.project.node.messages.Message;

public class PingMessage extends Message {

    public PingMessage() {
    }

    public PingMessage(int sender, int receiver) {
        super(sender, receiver);
    }
}

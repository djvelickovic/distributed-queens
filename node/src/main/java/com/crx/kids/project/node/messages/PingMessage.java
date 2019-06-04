package com.crx.kids.project.node.messages;

public class PingMessage extends DirectMessage {

    public PingMessage() {
    }

    public PingMessage(int sender, int receiver) {
        super(sender, receiver);
    }
}

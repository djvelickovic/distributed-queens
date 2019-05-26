package com.crx.kids.project.node.messages.newbie;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.Message;

import java.util.Map;

public class NewbieAcceptedMessage extends Message {
    private Map<Integer, NodeInfo> routingTable;

    public NewbieAcceptedMessage() {
    }

    public NewbieAcceptedMessage(int sender, int receiver, Map<Integer, NodeInfo> routingTable) {
        super(sender, receiver);
        this.routingTable = routingTable;
    }

    public Map<Integer, NodeInfo> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Integer, NodeInfo> routingTable) {
        this.routingTable = routingTable;
    }
}

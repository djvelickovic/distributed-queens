package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;

public class FullNodeInfo extends NodeInfo {
    private int id;

    public FullNodeInfo() {
    }

    public FullNodeInfo(int id, String ip, int port) {
        super(ip, port);
        this.id = id;
    }

    public FullNodeInfo(int id, NodeInfo nodeInfo) {
        super(nodeInfo.getIp(), nodeInfo.getPort());
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

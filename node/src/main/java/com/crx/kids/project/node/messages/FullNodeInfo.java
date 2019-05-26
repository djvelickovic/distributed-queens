package com.crx.kids.project.node.messages;

import com.crx.kids.project.common.NodeInfo;

public class FullNodeInfo extends NodeInfo {
    private Integer id;

    public FullNodeInfo(Integer id, String ip, int port) {
        super(ip, port);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

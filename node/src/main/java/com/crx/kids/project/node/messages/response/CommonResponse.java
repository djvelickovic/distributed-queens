package com.crx.kids.project.node.messages.response;

import com.crx.kids.project.common.NodeInfo;

public class CommonResponse {
    private CommonType type;
    private NodeInfo redirectNode;
    private String comment;

    public CommonResponse() {
    }

    public CommonResponse(CommonType type) {
        this.type = type;
    }

    public CommonType getType() {
        return type;
    }

    public void setType(CommonType type) {
        this.type = type;
    }

    public NodeInfo getRedirectNode() {
        return redirectNode;
    }

    public void setRedirectNode(NodeInfo redirectNode) {
        this.redirectNode = redirectNode;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

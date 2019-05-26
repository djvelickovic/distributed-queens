package com.crx.kids.project.node.messages.response;

import java.util.Map;

public class CommonResponse {
    private CommonType type;
    private Map<String, String> parameters;

    public CommonResponse() {
    }

    public CommonResponse(CommonType type, Map<String, String> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public CommonType getType() {
        return type;
    }

    public void setType(CommonType type) {
        this.type = type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "CommonResponse{" +
                "type=" + type +
                ", parameters=" + parameters +
                '}';
    }
}

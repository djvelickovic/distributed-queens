package com.crx.kids.project.node.endpoints.dto;

public class ControlPlaneResponse {

    private String code;
    private String message;

    public ControlPlaneResponse() {
    }

    public ControlPlaneResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

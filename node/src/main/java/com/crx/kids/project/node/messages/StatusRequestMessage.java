package com.crx.kids.project.node.messages;

public class StatusRequestMessage extends DirectMessage {

    private String statusRequestId;

    public StatusRequestMessage() {
    }

    public StatusRequestMessage(int sender, int receiver, String statusRequestId) {
        super(sender, receiver);
        this.statusRequestId = statusRequestId;
    }


    public String getStatusRequestId() {
        return statusRequestId;
    }

    public void setStatusRequestId(String statusRequestId) {
        this.statusRequestId = statusRequestId;
    }

    @Override
    public String toString() {
        return "StatusRequestMessage{" +
                "statusRequestId='" + statusRequestId + '\'' +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

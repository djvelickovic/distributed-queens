package com.crx.kids.project.node.messages;

import java.util.List;
import java.util.Map;

public class StatusMessage extends Message {

    private List<JobState> jobStates;
    private String statusRequestId;

    public StatusMessage() {
    }

    public StatusMessage(int sender, int receiver, String statusRequestId, List<JobState> jobStates) {
        super(sender, receiver);
        this.jobStates = jobStates;
        this.statusRequestId = statusRequestId;
    }


    public List<JobState> getJobStates() {
        return jobStates;
    }

    public void setJobStates(List<JobState> jobStates) {
        this.jobStates = jobStates;
    }

    public String getStatusRequestId() {
        return statusRequestId;
    }

    public void setStatusRequestId(String statusRequestId) {
        this.statusRequestId = statusRequestId;
    }

    @Override
    public String toString() {
        return "StatusMessage{" +
                "jobStates=" + jobStates +
                ", statusRequestId=" + statusRequestId +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

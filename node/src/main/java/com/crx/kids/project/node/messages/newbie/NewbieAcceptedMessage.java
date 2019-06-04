package com.crx.kids.project.node.messages.newbie;

import com.crx.kids.project.node.messages.DirectMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.Message;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewbieAcceptedMessage extends DirectMessage {
    // first must be smaller than second by id
    private FullNodeInfo firstNeighbour;
    private FullNodeInfo secondNeighbour;
    private Map<Integer, Map<Integer, List<Integer[]>>> collectedResults;
    private Set<Integer> finishedJobs;
    private Integer currentlyActiveDim;

    public NewbieAcceptedMessage() {
    }

    public NewbieAcceptedMessage(int sender, int receiver, FullNodeInfo firstNeighbour, FullNodeInfo secondNeighbour, Map<Integer, Map<Integer, List<Integer[]>>> collectedResults, Set<Integer> finishedJobs, Integer currentlyActiveDim) {
        super(sender, receiver);
        this.firstNeighbour = firstNeighbour;
        this.secondNeighbour = secondNeighbour;
        this.collectedResults = collectedResults;
        this.finishedJobs = finishedJobs;
        this.currentlyActiveDim = currentlyActiveDim;
    }

    public Set<Integer> getFinishedJobs() {
        return finishedJobs;
    }

    public void setFinishedJobs(Set<Integer> finishedJobs) {
        this.finishedJobs = finishedJobs;
    }

    public Map<Integer, Map<Integer, List<Integer[]>>> getCollectedResults() {
        return collectedResults;
    }

    public void setCollectedResults(Map<Integer, Map<Integer, List<Integer[]>>> collectedResults) {
        this.collectedResults = collectedResults;
    }

    public Integer getCurrentlyActiveDim() {
        return currentlyActiveDim;
    }

    public void setCurrentlyActiveDim(Integer currentlyActiveDim) {
        this.currentlyActiveDim = currentlyActiveDim;
    }

    public FullNodeInfo getFirstNeighbour() {
        return firstNeighbour;
    }

    public void setFirstNeighbour(FullNodeInfo firstNeighbour) {
        this.firstNeighbour = firstNeighbour;
    }

    public FullNodeInfo getSecondNeighbour() {
        return secondNeighbour;
    }

    public void setSecondNeighbour(FullNodeInfo secondNeighbour) {
        this.secondNeighbour = secondNeighbour;
    }
}

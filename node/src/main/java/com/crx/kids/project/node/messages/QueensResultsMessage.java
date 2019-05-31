package com.crx.kids.project.node.messages;

import com.crx.kids.project.node.entities.QueensResult;

import java.util.List;
import java.util.UUID;

public class QueensResultsMessage extends BroadcastMessage<String> {
    private List<QueensResult> results;
    private int dimension;

    public QueensResultsMessage() {
    }

    public QueensResultsMessage(int sender,int dimension, List<QueensResult> results) {
        super(sender, UUID.randomUUID().toString());
        this.results = results;
        this.dimension = dimension;
    }

    public List<QueensResult> getResults() {
        return results;
    }

    public void setResults(List<QueensResult> results) {
        this.results = results;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "QueensResultsMessage{" +
                "results=" + results +
                ", dimension=" + dimension +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

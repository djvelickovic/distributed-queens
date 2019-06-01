package com.crx.kids.project.node.messages;

import com.crx.kids.project.node.entities.QueensResult;

import java.util.List;
import java.util.UUID;

public class QueensResultBroadcast extends BroadcastMessage<String> {

    private int dimension;
    private List<QueensResult> queensResults;

    public QueensResultBroadcast() {
    }

    public QueensResultBroadcast(int sender, int dimension, List<QueensResult> queensResults) {
        super(sender, UUID.randomUUID().toString());
        this.dimension = dimension;
        this.queensResults = queensResults;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public List<QueensResult> getQueensResults() {
        return queensResults;
    }

    public void setQueensResults(List<QueensResult> queensResults) {
        this.queensResults = queensResults;
    }

    @Override
    public String toString() {
        return "QueensResultBroadcast{" +
                "dimension=" + dimension +
                ", queensResults=" + queensResults.size() +
                ", sender=" + sender +
                ", receiver=" + receiver +
                '}';
    }
}

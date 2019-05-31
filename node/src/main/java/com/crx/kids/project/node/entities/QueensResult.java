package com.crx.kids.project.node.entities;

import java.util.List;

public class QueensResult {
    private QueensJob queensJob;
    private List<Integer[]> results;

    public QueensResult() {
    }

    public QueensResult(QueensJob queensJob, List<Integer[]> results) {
        this.queensJob = queensJob;
        this.results = results;
    }

    public QueensJob getQueensJob() {
        return queensJob;
    }

    public void setQueensJob(QueensJob queensJob) {
        this.queensJob = queensJob;
    }

    public List<Integer[]> getResults() {
        return results;
    }

    public void setResults(List<Integer[]> results) {
        this.results = results;
    }
}

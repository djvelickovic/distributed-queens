package com.crx.kids.project.node.common;

public class Ghost {

    private Network network;
    private Jobs jobs;
    private CriticalSection criticalSection;
    private Configuration configuration;

    public Ghost() {
        network = new Network();
        jobs = new Jobs();
        criticalSection = new CriticalSection();
        configuration = new Configuration();
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Jobs getJobs() {
        return jobs;
    }

    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    public CriticalSection getCriticalSection() {
        return criticalSection;
    }

    public void setCriticalSection(CriticalSection criticalSection) {
        this.criticalSection = criticalSection;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}

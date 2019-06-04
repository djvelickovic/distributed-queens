package com.crx.kids.project.node.common.dto;

public class GhostDTO {
    private ConfigurationDTO configurationDTO;
    private CriticalSectionDTO criticalSectionDTO;
    private JobsDTO jobsDTO;
    private NetworkDTO networkDTO;

    public ConfigurationDTO getConfigurationDTO() {
        return configurationDTO;
    }

    public void setConfigurationDTO(ConfigurationDTO configurationDTO) {
        this.configurationDTO = configurationDTO;
    }

    public CriticalSectionDTO getCriticalSectionDTO() {
        return criticalSectionDTO;
    }

    public void setCriticalSectionDTO(CriticalSectionDTO criticalSectionDTO) {
        this.criticalSectionDTO = criticalSectionDTO;
    }

    public JobsDTO getJobsDTO() {
        return jobsDTO;
    }

    public void setJobsDTO(JobsDTO jobsDTO) {
        this.jobsDTO = jobsDTO;
    }

    public NetworkDTO getNetworkDTO() {
        return networkDTO;
    }

    public void setNetworkDTO(NetworkDTO networkDTO) {
        this.networkDTO = networkDTO;
    }
}

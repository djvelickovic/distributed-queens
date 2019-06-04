package com.crx.kids.project.node.common;

import com.crx.kids.project.node.common.dto.ConfigurationDTO;

public class Configuration {
    private Integer id;
    private Double limit = 0.1;

    public Configuration() {
    }

    public Configuration(ConfigurationDTO configurationDTO) {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLimit() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }
}

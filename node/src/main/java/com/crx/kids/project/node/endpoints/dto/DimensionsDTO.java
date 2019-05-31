package com.crx.kids.project.node.endpoints.dto;

public class DimensionsDTO {
    private Integer dimension;

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "DimensionsDTO{" +
                "dimension=" + dimension +
                '}';
    }
}

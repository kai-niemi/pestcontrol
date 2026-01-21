package io.cockroachdb.pest.model.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "desc",
        "buildInfo",
        "startedAt",
        "updatedAt",
        "metrics",
        "storeStatuses",
        "args",
        "env",
        "latencies",
        "activity",
        "totalSystemMemory",
        "numCpus"
})
public class NodeStatus {
    @JsonProperty("desc")
    @Valid
    private Desc desc;

    @JsonProperty("buildInfo")
    @Valid
    private BuildInfo buildInfo;

    @JsonProperty("startedAt")
    private String startedAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("totalSystemMemory")
    private String totalSystemMemory;

    @JsonProperty("numCpus")
    private Integer numCpus;

    @JsonProperty("desc")
    public Desc getDesc() {
        return desc;
    }

    @JsonProperty("desc")
    public void setDesc(Desc desc) {
        this.desc = desc;
    }

    @JsonProperty("buildInfo")
    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    @JsonProperty("buildInfo")
    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    @JsonProperty("startedAt")
    public String getStartedAt() {
        return startedAt;
    }

    @JsonProperty("startedAt")
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updatedAt")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("totalSystemMemory")
    public String getTotalSystemMemory() {
        return totalSystemMemory;
    }

    @JsonProperty("totalSystemMemory")
    public void setTotalSystemMemory(String totalSystemMemory) {
        this.totalSystemMemory = totalSystemMemory;
    }

    @JsonProperty("numCpus")
    public Integer getNumCpus() {
        return numCpus;
    }

    @JsonProperty("numCpus")
    public void setNumCpus(Integer numCpus) {
        this.numCpus = numCpus;
    }
}

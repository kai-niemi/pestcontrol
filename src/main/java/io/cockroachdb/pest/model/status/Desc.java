package io.cockroachdb.pest.model.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "nodeId",
        "address",
        "attrs",
        "locality",
        "ServerVersion",
        "buildTag",
        "startedAt",
        "localityAddress",
        "clusterName",
        "sqlAddress",
        "httpAddress"
})

public class Desc {
    @JsonProperty("nodeId")
    private Integer nodeId;

    @JsonProperty("address")
    @Valid
    private Address address;

    @JsonProperty("attrs")
    @Valid
    private Attrs attrs;

    @JsonProperty("locality")
    @Valid
    private NodeLocality locality;

    @JsonProperty("ServerVersion")
    @Valid
    private ServerVersion serverVersion;

    @JsonProperty("buildTag")
    private String buildTag;

    @JsonProperty("startedAt")
    private String startedAt;

    @JsonProperty("localityAddress")
    @Valid
    private List<Object> localityAddress;

    @JsonProperty("clusterName")
    private String clusterName;

    @JsonProperty("sqlAddress")
    @Valid
    private Address sqlAddress;

    @JsonProperty("httpAddress")
    @Valid
    private Address httpAddress;

    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("nodeId")
    public Integer getNodeId() {
        return nodeId;
    }

    @JsonProperty("nodeId")
    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("attrs")
    public Attrs getAttrs() {
        return attrs;
    }

    @JsonProperty("attrs")
    public void setAttrs(Attrs attrs) {
        this.attrs = attrs;
    }

    @JsonProperty("locality")
    public NodeLocality getLocality() {
        return locality;
    }

    @JsonProperty("locality")
    public void setLocality(NodeLocality locality) {
        this.locality = locality;
    }

    @JsonProperty("ServerVersion")
    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    @JsonProperty("ServerVersion")
    public void setServerVersion(ServerVersion serverVersion) {
        this.serverVersion = serverVersion;
    }

    @JsonProperty("buildTag")
    public String getBuildTag() {
        return buildTag;
    }

    @JsonProperty("buildTag")
    public void setBuildTag(String buildTag) {
        this.buildTag = buildTag;
    }

    @JsonProperty("startedAt")
    public String getStartedAt() {
        return startedAt;
    }

    @JsonProperty("startedAt")
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("localityAddress")
    public List<Object> getLocalityAddress() {
        return localityAddress;
    }

    @JsonProperty("localityAddress")
    public void setLocalityAddress(List<Object> localityAddress) {
        this.localityAddress = localityAddress;
    }

    @JsonProperty("clusterName")
    public String getClusterName() {
        return clusterName;
    }

    @JsonProperty("clusterName")
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @JsonProperty("sqlAddress")
    public Address getSqlAddress() {
        return sqlAddress;
    }

    @JsonProperty("sqlAddress")
    public void setSqlAddress(Address sqlAddress) {
        this.sqlAddress = sqlAddress;
    }

    @JsonProperty("httpAddress")
    public Address getHttpAddress() {
        return httpAddress;
    }

    @JsonProperty("httpAddress")
    public void setHttpAddress(Address httpAddress) {
        this.httpAddress = httpAddress;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

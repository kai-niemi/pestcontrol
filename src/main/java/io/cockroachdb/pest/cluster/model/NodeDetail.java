package io.cockroachdb.pest.cluster.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.Locality;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeDetail {
    public static NodeDetail from(Cluster.Node node) {
        NodeDetail nodeDetail = new NodeDetail();
        nodeDetail.setNodeId(node.getId());
        nodeDetail.setLocality(Locality.fromTiers(node.getLocality()));

        Address address = new Address();
        address.setAddressField(node.getListenAddr());
        address.setNetworkField(node.getListenAddr());
        nodeDetail.setAddress(address);

        return nodeDetail;
    }

    @JsonProperty("node_id")
    private Integer nodeId;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("locality")
    private Locality locality;

    @JsonProperty("ServerVersion")
    private ServerVersion serverVersion;

    @JsonProperty("build_tag")
    private String buildTag;

    @JsonProperty("started_at")
    private Long startedAt;

    @JsonProperty("cluster_name")
    private String clusterName;

    @JsonProperty("sql_address")
    private SqlAddress sqlAddress;

    @JsonProperty("total_system_memory")
    private Long totalSystemMemory;

    @JsonProperty("num_cpus")
    private Integer numCpus;

    @JsonProperty("updated_at")
    private Long updatedAt;

    @JsonProperty("liveness_status")
    private Integer livenessStatus;

    @JsonProperty("args")
    private List<String> args;

    @JsonProperty("env")
    private List<String> env;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public String getSqlAddressPort() {
        String[] parts = getSqlAddress().getAddressField().split(":");
        return parts.length > 1 ? parts[1] : getSqlAddress().getAddressField();
    }

    @JsonProperty("args")
    public void setArgs(List<String> args) {
        this.args = args;
    }

    @JsonProperty("env")
    public void setEnv(List<String> env) {
        this.env = env;
    }

    @JsonProperty("args")
    public List<String> getArgs() {
        return args;
    }

    @JsonProperty("env")
    public List<String> getEnv() {
        return env;
    }

    @JsonProperty("node_id")
    public Integer getNodeId() {
        return nodeId;
    }

    @JsonProperty("node_id")
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

    @JsonProperty("locality")
    public Locality getLocality() {
        return locality;
    }

    @JsonProperty("locality")
    public void setLocality(Locality locality) {
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

    @JsonProperty("build_tag")
    public String getBuildTag() {
        return buildTag;
    }

    @JsonProperty("build_tag")
    public void setBuildTag(String buildTag) {
        this.buildTag = buildTag;
    }

    @JsonProperty("started_at")
    public Long getStartedAt() {
        return startedAt;
    }

    @JsonProperty("started_at")
    public void setStartedAt(Long startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("cluster_name")
    public String getClusterName() {
        return clusterName;
    }

    @JsonProperty("cluster_name")
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @JsonProperty("sql_address")
    public SqlAddress getSqlAddress() {
        return sqlAddress;
    }

    @JsonProperty("sql_address")
    public void setSqlAddress(SqlAddress sqlAddress) {
        this.sqlAddress = sqlAddress;
    }

    @JsonProperty("total_system_memory")
    public Long getTotalSystemMemory() {
        return totalSystemMemory;
    }

    @JsonProperty("total_system_memory")
    public void setTotalSystemMemory(Long totalSystemMemory) {
        this.totalSystemMemory = totalSystemMemory;
    }

    @JsonProperty("num_cpus")
    public Integer getNumCpus() {
        return numCpus;
    }

    @JsonProperty("num_cpus")
    public void setNumCpus(Integer numCpus) {
        this.numCpus = numCpus;
    }

    @JsonProperty("updated_at")
    public Long getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("liveness_status")
    public Integer getLivenessStatus() {
        return livenessStatus;
    }

    @JsonProperty("liveness_status")
    public void setLivenessStatus(Integer livenessStatus) {
        this.livenessStatus = livenessStatus;
    }

//    @JsonAnyGetter
//    public Map<String, Object> getAdditionalProperties() {
//        return this.additionalProperties;
//    }
//
//    @JsonAnySetter
//    public void setAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//    }

    @Override
    public String toString() {
        return "NodeDetail{" +
               "additionalProperties=" + additionalProperties +
               ", nodeId=" + nodeId +
               ", address=" + address +
               ", locality=" + locality +
               ", serverVersion=" + serverVersion +
               ", buildTag='" + buildTag + '\'' +
               ", startedAt=" + startedAt +
               ", clusterName='" + clusterName + '\'' +
               ", sqlAddress=" + sqlAddress +
               ", totalSystemMemory=" + totalSystemMemory +
               ", numCpus=" + numCpus +
               ", updatedAt=" + updatedAt +
               ", livenessStatus=" + livenessStatus +
               '}';
    }
}

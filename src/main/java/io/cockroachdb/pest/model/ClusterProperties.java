package io.cockroachdb.pest.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.util.Networking;

/**
 * Connection properties for connecting to a CockroachDB cluster.
 */
@Validated
public class ClusterProperties {
    @NotNull
    @NotBlank
    private String clusterId;

    @NotNull
    @NotBlank
    private String clusterName;

    @Valid
    @NotNull
    private ClusterType clusterType;

    private String adminUrl;

    private String apiKey;

    private Path certificatePath;

    @Valid
    private DataSourceProperties dataSourceProperties;

    private List<@Valid NodeProperties> nodes = new ArrayList<>();

    private String version;

    public void init() {
        AtomicInteger id = new AtomicInteger();
        nodes.forEach(properties -> properties.setId(id.incrementAndGet()));
    }

    public NodeProperties findNodePropertiesById(int nodeId) {
        return nodes.stream()
                .filter(agent -> Objects.equals(nodeId, agent.getId()))
                .findFirst()
                .orElseThrow();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<NodeProperties> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeProperties> nodes) {
        this.nodes = nodes;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public Path getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(Path certificatePath) {
        this.certificatePath = certificatePath;
    }

    public DataSourceProperties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public String getAdminUrl() {
        return Networking.resolve(adminUrl);
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}

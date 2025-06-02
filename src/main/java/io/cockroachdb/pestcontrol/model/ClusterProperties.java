package io.cockroachdb.pestcontrol.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pestcontrol.util.Networking;

/**
 * Connection properties for connecting to a CockroachDB cluster.
 */
@Validated
public class ClusterProperties {
    @Valid
    @NotNull
    private String clusterId;

    @Valid
    @NotNull
    private ClusterType clusterType;

    private String adminUrl;

    private String apiKey;

    private Path certificatePath;

    private DataSourceProperties dataSourceProperties;

    @Valid
    private List<NodeProperties> nodes = new ArrayList<>();

    public NodeProperties findNodeProperties(Integer nodeId) {
        return nodes.stream()
                .filter(agent -> nodeId.equals(agent.getId()))
                .findFirst()
                .orElseThrow();
    }

    public void init() {
        AtomicInteger id = new AtomicInteger();
        nodes.forEach(properties -> properties.setId(id.incrementAndGet()));
    }

    public List<NodeProperties> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeProperties> nodes) {
        this.nodes = nodes;
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
        return new PropertyPlaceholderHelper("${", "}")
                .replacePlaceholders(adminUrl,
                        placeholderName -> switch (placeholderName) {
                            case "local-ip" -> Networking.getLocalIP();
                            case "public-ip" -> Networking.getPublicIP();
                            default -> placeholderName;
                        });
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

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}

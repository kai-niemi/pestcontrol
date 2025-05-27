package io.cockroachdb.pestcontrol.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.validation.annotation.Validated;

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
    private List<MachineProperties> machines = new ArrayList<>();

    public MachineProperties getNodeById(Integer nodeId) {
        return machines.stream()
                .filter(agent -> nodeId.equals(agent.getId()))
                .findFirst()
                .orElseThrow();
    }

    public void init() {
        AtomicInteger id = new AtomicInteger();
        machines.forEach(agentProperties ->
                agentProperties.setId(id.incrementAndGet()));
    }

    public List<MachineProperties> getMachines() {
        return machines;
    }

    public void setMachines(List<MachineProperties> machines) {
        this.machines = machines;
    }

    public String getUsername() {
        return dataSourceProperties.getUsername();
    }

    public String getPassword() {
        return dataSourceProperties.getPassword();
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

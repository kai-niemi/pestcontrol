package io.cockroachdb.pc.model;

import java.nio.file.Path;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.util.PropertyPlaceholderHelper;

import io.cockroachdb.pc.schema.ClusterType;
import io.cockroachdb.pc.util.Networking;

/**
 * Connection properties for connecting to a CockroachDB cluster.
 */
public class ClusterProperties {
    private String clusterId;

    private ClusterType clusterType;

    private String adminUrl;

    private String apiKey;

    private Path certificatePath;

    private DataSourceProperties dataSourceProperties;

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

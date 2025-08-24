package io.cockroachdb.pest.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.util.Networking;

/**
 * Connection properties for connecting to a CockroachDB cluster.
 */
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clusterId", "clusterName", "clusterType", "version",
        "adminUrl", "apiKey", "secure", "dataSourceProperties", "nodes"})
public class ClusterSettings {
    @NotNull
    @NotBlank
    private String clusterId;

    @NotNull
    @NotBlank
    private String clusterName;

    @Valid
    @NotNull
    private ClusterType clusterType;

    @Valid
    @JsonIgnoreProperties({"xa", "generateUniqueName"})
    private DataSourceProperties dataSourceProperties;

    @Valid
    private BaselineSettings baseline;

    private List<@Valid NodeSettings> nodes = new ArrayList<>();

    private String version;

    private String adminUrl;

    private String apiKey;

    private Path certificatePath;

    private boolean secure;

    public void init() {
        AtomicInteger id = new AtomicInteger();

        nodes.forEach(nodeSettings -> {
            id.incrementAndGet();

            if (Objects.isNull(nodeSettings.getId())) {
                nodeSettings.setId(id.get());
            }
            if (Objects.isNull(nodeSettings.getName())) {
                nodeSettings.setName("n%d".formatted(id.get()));
            }

            nodeSettings.init(baseline, id.get());

            if (!Objects.isNull(baseline)) {
                nodeSettings.init(baseline, id.get());
            }

            nodeSettings.resolvePlaceholders();
        });

        adminUrl = Networking.resolve(adminUrl);
    }

    public NodeSettings findNodePropertiesById(int nodeId) {
        return nodes.stream()
                .filter(x -> Objects.equals(nodeId, x.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node id not found: " + nodeId));
    }

    public BaselineSettings getBaseline() {
        return baseline;
    }

    public void setBaseline(BaselineSettings baseline) {
        this.baseline = baseline;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<NodeSettings> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeSettings> nodes) {
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
        return adminUrl;
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

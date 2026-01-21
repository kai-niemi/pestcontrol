package io.cockroachdb.pest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@JsonPropertyOrder({
        "clusterId", "clusterName", "clusterType",
        "adminUrl", "apiKey", "secure", "dataSourceProperties",
        "version", "loadBalancer", "baseline", "nodes"})
public class Cluster {
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

    private BaseLine baseLine = new BaseLine();

    private Cluster.HAProxy haProxy = new HAProxy();

    private List<@Valid Node> nodes = new ArrayList<>();

    // Base URL for L7 load balancer or primary node
    private String adminUrl;

    // Cloud API key for bearer token for Cockroach Cloud only
    private String apiKey;

    // Cluster API authentication key for Cockroach Cloud only
    private String authToken;

    private boolean toxiProxyEnabled;

    public void postConstruct(ApplicationProperties applicationProperties) {
        this.toxiProxyEnabled = applicationProperties.getToxiProxy().isEnabled();

        if (!baseLine.getInternalIps().isEmpty()) {
            Assert.state(baseLine.getInternalIps().size() == nodes.size(),
                    "internal-ip count differs from node count");
        }

        this.nodes.forEach(x -> {
            x.postConstruct(baseLine);
            baseLine.incrementId();
        });

        this.adminUrl = NetworkAddress.resolve(adminUrl);
    }

    public Node getNodeById(int nodeId) {
        return nodes.stream()
                .filter(x -> Objects.equals(nodeId, x.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node id not found: " + nodeId));
    }

    public boolean isToxiProxyEnabled() {
        return toxiProxyEnabled;
    }

    @JsonIgnore
    public boolean isSecure() {
        return ClusterTypes.isSecure(clusterType);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cluster cluster = (Cluster) o;
        return clusterId.equals(cluster.clusterId);
    }

    @Override
    public int hashCode() {
        return clusterId.hashCode();
    }

    //--------------------------------------------------------

    public HAProxy getHaProxy() {
        return haProxy;
    }

    public void setHaProxy(HAProxy haProxy) {
        this.haProxy = haProxy;
    }

    public BaseLine getBaseLine() {
        return baseLine;
    }

    public void setBaseLine(BaseLine baseLine) {
        this.baseLine = baseLine;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
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

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Validated
    public static class BaseLine {
        private final AtomicInteger id = new AtomicInteger();
        @NotNull
        private String serviceAddr;
        private String listenAddr;
        private String advertiseAddr;
        private String advertiseProxyAddr;
        private String sqlAddr;
        private String httpAddr;
        private List<String> certHosts = List.of();
        private List<String> internalIps = new ArrayList<>();

        private String version;

        public BaseLine incrementId() {
            id.incrementAndGet();
            return this;
        }

        public Integer currentId() {
            return id.get();
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getInternalIps() {
            return internalIps;
        }

        public void setInternalIps(List<String> internalIps) {
            this.internalIps = internalIps;
        }

        public List<String> getCertHosts() {
            return certHosts;
        }

        public void setCertHosts(List<String> certHosts) {
            this.certHosts = certHosts;
        }

        public String getServiceAddr() {
            return serviceAddr;
        }

        public void setServiceAddr(String serviceAddr) {
            this.serviceAddr = serviceAddr;
        }

        public String getListenAddr() {
            return listenAddr;
        }

        public void setListenAddr(String listenAddr) {
            this.listenAddr = listenAddr;
        }

        public String getAdvertiseAddr() {
            return advertiseAddr;
        }

        public void setAdvertiseAddr(String advertiseAddr) {
            this.advertiseAddr = advertiseAddr;
        }

        public String getAdvertiseProxyAddr() {
            return advertiseProxyAddr;
        }

        public void setAdvertiseProxyAddr(String advertiseProxyAddr) {
            this.advertiseProxyAddr = advertiseProxyAddr;
        }

        public String getSqlAddr() {
            return sqlAddr;
        }

        public void setSqlAddr(String sqlAddr) {
            this.sqlAddr = sqlAddr;
        }

        public String getHttpAddr() {
            return httpAddr;
        }

        public void setHttpAddr(String httpAddr) {
            this.httpAddr = httpAddr;
        }
    }

    @Validated
    public static class HAProxy {
        @NotNull
        private String rpcAddr;

        @NotNull
        private String httpAddr;

        @NotNull
        private String statsAddr;

        public String getRpcAddr() {
            return rpcAddr;
        }

        public void setRpcAddr(String rpcAddr) {
            this.rpcAddr = rpcAddr;
        }

        public String getHttpAddr() {
            return httpAddr;
        }

        public void setHttpAddr(String httpAddr) {
            this.httpAddr = httpAddr;
        }

        public String getStatsAddr() {
            return statsAddr;
        }

        public void setStatsAddr(String statsAddr) {
            this.statsAddr = statsAddr;
        }
    }
}

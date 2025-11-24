package io.cockroachdb.pest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.cluster.InvalidConfigurationException;
import io.cockroachdb.pest.util.NetworkAddress;

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

    private LoadBalancer loadBalancer = new LoadBalancer();

    private List<@Valid Node> nodes = new ArrayList<>();

    private String adminUrl;

    private String apiKey;

    public void postConstruct() {
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

    @JsonIgnore
    public boolean isSecure() {
        return ClusterTypes.isSecure(clusterType);
    }

    //--------------------------------------------------------

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
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

    @Validated
    public static class BaseLine {
        @NotNull
        private String serviceAddr;

        private String listenAddr;

        private String advertiseAddr;

        private String advertiseProxyAddr;

        private String sqlAddr;

        private String httpAddr;

        private List<String> certHosts = List.of();

        private final AtomicInteger id = new AtomicInteger();

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

        public void setCertHosts(List<String> certHosts) {
            this.certHosts = certHosts;
        }
    }

    /**
     * Node properties describing a local or remote network node.
     */
    @Validated
    @JsonPropertyOrder({
            "locality", "id", "name", "url",
            "serviceUrl", "listenAddr", "advertiseAddr", "advertiseProxyAddr",
            "sqlAddr", "httpAddr", "certHosts"})
    public static class Node {
        @NotNull
        private String locality;

        private Integer id;

        private String name;

        private String serviceAddr;

        private String listenAddr;

        private String advertiseAddr;

        private String advertiseProxyAddr;

        private String sqlAddr;

        private String httpAddr;

        private List<String> certHosts = List.of();

        private String version;

        public void postConstruct(BaseLine baseline) {
            if (Objects.isNull(serviceAddr)) {
                serviceAddr = NetworkAddress.from(baseline.getServiceAddr(),
                                baseline.getInternalIps(), baseline.currentId())
                        .toAddressString();
            }
            if (Objects.isNull(sqlAddr)) {
                sqlAddr = NetworkAddress.from(baseline.getSqlAddr(),
                                baseline.getInternalIps(), baseline.currentId())
                        .toAddressString();
            }
            if (Objects.isNull(httpAddr)) {
                httpAddr = NetworkAddress.from(baseline.getHttpAddr(),
                                baseline.getInternalIps(), baseline.currentId())
                        .toAddressString();
            }
            if (Objects.isNull(listenAddr)) {
                listenAddr = NetworkAddress.from(baseline.getListenAddr(),
                                baseline.getInternalIps(), baseline.currentId())
                        .toAddressString();
            }
            if (Objects.isNull(advertiseAddr)) {
                advertiseAddr = NetworkAddress.from(baseline.getAdvertiseAddr(),
                                baseline.getInternalIps(), baseline.currentId())
                        .toAddressString();
            }
            if (Objects.isNull(advertiseProxyAddr)) {
                advertiseProxyAddr = NetworkAddress.from(baseline.getAdvertiseProxyAddr(),
                                baseline.getInternalIps(), baseline.currentId())
                        .toAddressString();
            }
            if (Objects.isNull(version)) {
                version = baseline.getVersion();
            }

            if (certHosts.isEmpty()) {
                setCertHosts(baseline.getCertHosts());
            }
            if (Objects.isNull(id)) {
                setId(baseline.currentId() + 1);
            }
            if (Objects.isNull(name)) {
                setName("n%d".formatted(id));
            }

            Assert.state(this.id > 0, "id must be > 0");
            Assert.notNull(this.sqlAddr, "sql-addr is required for node " + this.id);
            Assert.notNull(this.serviceAddr, "service-addr is required for node " + this.id);

            // Resolve any placeholders
            this.serviceAddr = NetworkAddress.resolve(serviceAddr);
            this.listenAddr = NetworkAddress.resolve(listenAddr);
            this.advertiseAddr = NetworkAddress.resolve(advertiseAddr);
            this.advertiseProxyAddr = NetworkAddress.resolve(advertiseProxyAddr);
            this.sqlAddr = NetworkAddress.resolve(sqlAddr);
            this.httpAddr = NetworkAddress.resolve(httpAddr);
            this.certHosts = this.certHosts.stream().map(NetworkAddress::resolve).toList();
        }

        @JsonIgnore
        public String getJoinAddress() {
            NetworkAddress advertiseAddr = NetworkAddress.from(this.advertiseAddr);
            if (advertiseAddr.getAddress().isPresent()) {
                if (advertiseAddr.getPort().isEmpty()) {
                    advertiseAddr = advertiseAddr.addPort(NetworkAddress.from(listenAddr).getPort().orElseThrow(
                            () -> new InvalidConfigurationException("Both advertise-addr " +
                                                                    "and listen-addr are missing port number for node " + getId())
                    ));
                }
                return advertiseAddr.toAddressString();
            }
            NetworkAddress listenAddr = NetworkAddress.from(this.listenAddr);
            if (listenAddr.getAddress().isPresent()) {
                return listenAddr.getAddress().get();
            }
            throw new InvalidConfigurationException("Both advertise-addr " +
                                                    "and listen-addr are missing for node " + getId());
        }

        @JsonIgnore
        public Link getServiceLink() {
            String path = Objects.requireNonNull(serviceAddr).endsWith("/api") ? serviceAddr : serviceAddr + "/api";
            return Link.of("%s://%s".formatted("http", path));
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getCertHosts() {
            return certHosts;
        }

        public void setCertHosts(List<String> certHosts) {
            this.certHosts = certHosts;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServiceAddr() {
            return serviceAddr;
        }

        public void setServiceAddr(String serviceAddr) {
            this.serviceAddr = serviceAddr;
        }

        public String getLocality() {
            return locality;
        }

        public void setLocality(String locality) {
            this.locality = locality;
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

        public String getHttpAddr() {
            return httpAddr;
        }

        public void setHttpAddr(String httpAddr) {
            this.httpAddr = httpAddr;
        }

        public String getSqlAddr() {
            return sqlAddr;
        }

        public void setSqlAddr(String sqlAddr) {
            this.sqlAddr = sqlAddr;
        }
    }

    @Validated
    public static class LoadBalancer {
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

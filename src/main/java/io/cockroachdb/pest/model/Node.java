package io.cockroachdb.pest.model;

import java.util.List;
import java.util.Objects;

import org.springframework.hateoas.Link;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotNull;

/**
 * Node properties describing a local or remote network node.
 */
@Validated
@JsonPropertyOrder({
        "locality", "id", "name", "url",
        "serviceUrl", "listenAddr", "advertiseAddr", "advertiseProxyAddr",
        "sqlAddr", "httpAddr", "certHosts", "version"})
public class Node {
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

    public void postConstruct(Cluster.BaseLine baseline) {
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

    public Link getServiceLink() {
        return Link.of("http://%s".formatted(Objects.requireNonNull(getServiceAddr())))
                .withRel(LinkRelations.SERVICE_REL);
    }

    @JsonIgnore
    public String getJoinAddress() {
        NetworkAddress advertiseAddr = NetworkAddress.from(this.advertiseAddr);
        if (advertiseAddr.getAddress().isPresent()) {
            if (advertiseAddr.getPort().isEmpty()) {
                advertiseAddr = advertiseAddr.addPort(NetworkAddress.from(listenAddr).getPort().orElseThrow(
                        () -> new IllegalStateException("Both advertise-addr " +
                                                        "and listen-addr are missing port number for node "
                                                        + getId())
                ));
            }
            return advertiseAddr.toAddressString();
        }
        NetworkAddress listenAddr = NetworkAddress.from(this.listenAddr);
        if (listenAddr.getAddress().isPresent()) {
            return listenAddr.getAddress().get();
        }
        throw new IllegalStateException("Both advertise-addr " +
                                        "and listen-addr are missing for node " + getId());
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

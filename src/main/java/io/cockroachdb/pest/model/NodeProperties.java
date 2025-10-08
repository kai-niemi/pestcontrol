package io.cockroachdb.pest.model;

import java.util.List;
import java.util.Objects;

import org.springframework.hateoas.Link;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.util.Networking;

/**
 * Node properties describing a local or remote network node.
 */
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "locality", "id", "name", "url",
        "serviceUrl", "listenAddr", "advertiseAddr", "advertiseProxyAddr",
        "sqlAddr", "httpAddr", "certHosts"})
public class NodeProperties {
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

    public void init(BaselineProperties baseline) {
        if (certHosts.isEmpty()) {
            setCertHosts(baseline.getCertHosts());
        }
        if (Objects.isNull(serviceAddr)) {
            setServiceAddr(Networking.incrementPort(baseline.getServiceAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(sqlAddr)) {
            setSqlAddr(Networking.incrementPort(baseline.getSqlAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(httpAddr)) {
            setHttpAddr(Networking.incrementPort(baseline.getHttpAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(listenAddr)) {
            setListenAddr(Networking.incrementPort(baseline.getListenAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(advertiseAddr)) {
            setAdvertiseAddr(Networking.incrementPort(baseline.getAdvertiseAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(advertiseProxyAddr)) {
            setAdvertiseProxyAddr(Networking.incrementPort(baseline.getAdvertiseProxyAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(id)) {
            setId(baseline.getCurrentId() + 1);
        }
        if (Objects.isNull(name)) {
            setName("n%d".formatted(id));
        }

        Assert.notNull(this.sqlAddr, "sql-addr is required for node " + this.id);
        Assert.notNull(this.serviceAddr, "service-addr is required for node " + this.id);
        Assert.state(this.listenAddr == null && this.advertiseAddr == null,
                "Both listen-addr and advertise-addr missing for node " + this.id);
        Assert.state(this.id > 0, "id must be > 0");

        // Resolve any placeholders
        this.serviceAddr = Networking.resolve(serviceAddr);
        this.listenAddr = Networking.resolve(listenAddr);
        this.advertiseAddr = Networking.resolve(advertiseAddr);
        this.advertiseProxyAddr = Networking.resolve(advertiseProxyAddr);
        this.sqlAddr = Networking.resolve(sqlAddr);
        this.httpAddr = Networking.resolve(httpAddr);
        this.certHosts = this.certHosts.stream().map(Networking::resolve).toList();
    }

    public Link getServiceLink() {
        String path = Objects.requireNonNull(serviceAddr).endsWith("/api") ? serviceAddr : serviceAddr + "/api";
        return Link.of("%s://%s".formatted("http", path));
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

    public String getRpcAddr() {
        String addr = this.listenAddr;
        if (Objects.isNull(addr)) {
            addr = this.advertiseAddr;
        }
        if (addr.startsWith(":")) {
            addr = "%s:%s".formatted(Networking.getCanonicalHostName(), addr.substring(1));
        }
        return addr;
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
        String addr = sqlAddr;
        if (addr.startsWith(":")) {
            addr = "%s:%s".formatted(Networking.getCanonicalHostName(), addr.substring(1));
        }
        return addr;
    }

    public void setSqlAddr(String sqlAddr) {
        this.sqlAddr = sqlAddr;
    }
}

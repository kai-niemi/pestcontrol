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
import static io.cockroachdb.pest.util.Networking.incrementPort;
import static io.cockroachdb.pest.util.Networking.resolve;

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
            setServiceAddr(incrementPort(baseline.getServiceAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(sqlAddr)) {
            setSqlAddr(incrementPort(baseline.getSqlAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(httpAddr)) {
            setHttpAddr(incrementPort(baseline.getHttpAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(listenAddr)) {
            setListenAddr(incrementPort(baseline.getListenAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(advertiseAddr)) {
            setAdvertiseAddr(incrementPort(baseline.getAdvertiseAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(advertiseProxyAddr)) {
            setAdvertiseProxyAddr(incrementPort(baseline.getAdvertiseProxyAddr(), baseline.getCurrentId()));
        }
        if (Objects.isNull(id)) {
            setId(baseline.getCurrentId() + 1);
        }
        if (Objects.isNull(name)) {
            setName("n%d".formatted(id));
        }

        // Resolve any placeholders

        this.serviceAddr = resolve(serviceAddr);
        this.listenAddr = resolve(listenAddr);
        this.advertiseAddr = resolve(advertiseAddr);
        this.advertiseProxyAddr = resolve(advertiseProxyAddr);
        this.sqlAddr = resolve(sqlAddr);
        this.httpAddr = resolve(httpAddr);
        this.certHosts = this.certHosts.stream().map(Networking::resolve).toList();

        Assert.notNull(this.id, "id is required");
        Assert.state(this.id>0, "id must be > 0");
        Assert.notNull(this.name, "name is required");
        Assert.notNull(this.serviceAddr, "service-addr is required");
        Assert.notNull(this.listenAddr, "listen-addr is required");
        Assert.notNull(this.advertiseAddr, "advertise-addr is required");
        Assert.notNull(this.advertiseProxyAddr, "advertise-proxy-addr is required");
        Assert.notNull(this.sqlAddr, "sql-addr is required");
        Assert.notNull(this.httpAddr, "http-addr is required");
    }

    public Link getAdminLink(boolean secure) {
        String path = Objects.requireNonNull(httpAddr);
        return Link.of("%s://%s".formatted(secure ? "https" : "http", path));
    }

    public Link getServiceLink(boolean secure) {
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

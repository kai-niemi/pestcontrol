package io.cockroachdb.pest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.hateoas.Link;
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
public class NodeSettings {
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

    public void init(BaselineSettings baseline, int id) {
        if (getCertHosts().isEmpty()) {
            setCertHosts(baseline.getCertHosts());
        }
        if (Objects.isNull(getServiceAddr())) {
            setServiceAddr(BaselineSettings.incrementPort(baseline.getServiceAddr(), id));
        }
        if (Objects.isNull(getSqlAddr())) {
            setSqlAddr(BaselineSettings.incrementPort(baseline.getSqlAddr(), id));
        }
        if (Objects.isNull(getHttpAddr())) {
            setHttpAddr(BaselineSettings.incrementPort(baseline.getHttpAddr(), id));
        }
        if (Objects.isNull(getListenAddr())) {
            setListenAddr(BaselineSettings.incrementPort(baseline.getListenAddr(), id));
        }
        if (Objects.isNull(getAdvertiseAddr())) {
            setAdvertiseAddr(BaselineSettings.incrementPort(baseline.getAdvertiseAddr(), id));
        }
        if (Objects.isNull(getAdvertiseProxyAddr())) {
            setAdvertiseProxyAddr(BaselineSettings.incrementPort(baseline.getAdvertiseProxyAddr(), id));
        }
    }

    public void resolvePlaceholders() {
        serviceAddr = Networking.resolve(serviceAddr);
        listenAddr = Networking.resolve(listenAddr);
        advertiseAddr = Networking.resolve(advertiseAddr);
        advertiseProxyAddr = Networking.resolve(advertiseProxyAddr);
        sqlAddr = Networking.resolve(sqlAddr);
        httpAddr = Networking.resolve(httpAddr);

        List<String> hosts = certHosts.stream().map(Networking::resolve).toList();
        certHosts.clear();
        certHosts.addAll(hosts);
    }

    public Link getAdminLink(boolean secure) {
        return Link.of("%s://%s".formatted(secure ? "https" : "http",
                Objects.requireNonNull(getHttpAddr())));
    }

    public Link getServiceLink(boolean secure) {
        String path = getServiceAddr();
        path = path.endsWith("/api") ? path : path + "/api";
        return Link.of("%s://%s".formatted(secure ? "https" : "http", path));
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

    //    @JsonProperty("listen-addr")
    public String getListenAddr() {
        return listenAddr;
    }

    public void setListenAddr(String listenAddr) {
        this.listenAddr = listenAddr;
    }

    //    @JsonProperty("advertise-addr")
    public String getAdvertiseAddr() {
        return advertiseAddr;
    }

    public void setAdvertiseAddr(String advertiseAddr) {
        this.advertiseAddr = advertiseAddr;
    }

    //    @JsonProperty("advertise-proxy-addr")
    public String getAdvertiseProxyAddr() {
        return advertiseProxyAddr;
    }

    public void setAdvertiseProxyAddr(String advertiseProxyAddr) {
        this.advertiseProxyAddr = advertiseProxyAddr;
    }

    //    @JsonProperty("http-addr")
    public String getHttpAddr() {
        return httpAddr;
    }

    public void setHttpAddr(String httpAddr) {
        this.httpAddr = httpAddr;
    }

    //    @JsonProperty("sql-addr")
    public String getSqlAddr() {
        return sqlAddr;
    }

    public void setSqlAddr(String sqlAddr) {
        this.sqlAddr = sqlAddr;
    }
}

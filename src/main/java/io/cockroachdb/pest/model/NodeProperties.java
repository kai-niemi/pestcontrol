package io.cockroachdb.pest.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.hateoas.Link;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.util.Networking;

/**
 * Node properties describing a local or remote network node.
 */
@Validated
@JsonPropertyOrder({
        "id", "name", "url", "locality",
        "listenAddr", "advertiseAddr", "advertiseProxyAddr",
        "sqlAddr", "httpAddr", "certHosts"})
public class NodeProperties {
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private String url;

    @NotNull
    private String locality;

    private String listenAddr;

    private String advertiseAddr;

    private String advertiseProxyAddr;

    private String sqlAddr;

    private String httpAddr;

    private List<String> certHosts = List.of();

    @JsonIgnore
    public Link getBaseUrl() {
        String path = (url.endsWith("/") ? "api" : "/api");
        return Link.of(Networking.resolve(url) + path);
    }

    public List<String> getCertHosts() {
        return certHosts.stream().map(Networking::resolve)
                .collect(Collectors.toList());
    }

    public void setCertHosts(List<String> certHosts) {
        this.certHosts = certHosts;
    }

    @JsonIgnore
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

    public String getUrl() {
        return Networking.resolve(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    @JsonProperty("listen-addr")
    public String getListenAddr() {
        return Networking.resolve(listenAddr);
    }

    public void setListenAddr(String listenAddr) {
        this.listenAddr = listenAddr;
    }

    @JsonProperty("advertise-addr")
    public String getAdvertiseAddr() {
        return Networking.resolve(advertiseAddr);
    }

    public void setAdvertiseAddr(String advertiseAddr) {
        this.advertiseAddr = advertiseAddr;
    }

    @JsonProperty("advertise-proxy-addr")
    public String getAdvertiseProxyAddr() {
        return advertiseProxyAddr;
    }

    public void setAdvertiseProxyAddr(String advertiseProxyAddr) {
        this.advertiseProxyAddr = advertiseProxyAddr;
    }

    @JsonProperty("http-addr")
    public String getHttpAddr() {
        return Networking.resolve(httpAddr);
    }

    public void setHttpAddr(String httpAddr) {
        this.httpAddr = httpAddr;
    }

    @JsonProperty("sql-addr")
    public String getSqlAddr() {
        return Networking.resolve(sqlAddr);
    }

    public void setSqlAddr(String sqlAddr) {
        this.sqlAddr = sqlAddr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeProperties that = (NodeProperties) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url) && Objects.equals(name, that.name)
               && Objects.equals(locality, that.locality) && Objects.equals(listenAddr, that.listenAddr)
               && Objects.equals(advertiseAddr, that.advertiseAddr) && Objects.equals(sqlAddr,
                that.sqlAddr) && Objects.equals(httpAddr, that.httpAddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, name, locality, listenAddr, advertiseAddr, sqlAddr, httpAddr);
    }

    @Override
    public String toString() {
        return "NodeProperties{" +
               "advertiseAddr='" + advertiseAddr + '\'' +
               ", id=" + id +
               ", url='" + url + '\'' +
               ", name='" + name + '\'' +
               ", locality='" + locality + '\'' +
               ", listenAddr='" + listenAddr + '\'' +
               ", sqlAddr='" + sqlAddr + '\'' +
               ", httpAddr='" + httpAddr + '\'' +
               '}';
    }
}

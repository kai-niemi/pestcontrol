package io.cockroachdb.pestcontrol.model;

import org.springframework.hateoas.Link;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;

/**
 * Node properties describing a local or remote remote Pest Control instance.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Validated
public class MachineProperties {
    private int id;

    @NotNull
    private String url;

    private String name;

    @NotNull
    private String locality;

    private String listenAddr;

    private String advertiseAddr;

    private String sqlAddr;

    private String httpAddr;

    @JsonIgnore
    public Link getBaseUrl() {
        String path = (url.endsWith("/") ? "api" : "/api");
        return Link.of(url + path);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAdvertiseAddr() {
        return advertiseAddr;
    }

    public void setAdvertiseAddr(String advertiseAddr) {
        this.advertiseAddr = advertiseAddr;
    }

    public String getHttpAddr() {
        return httpAddr;
    }

    public void setHttpAddr(String httpAddr) {
        this.httpAddr = httpAddr;
    }

    public String getListenAddr() {
        return listenAddr;
    }

    public void setListenAddr(String listenAddr) {
        this.listenAddr = listenAddr;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getSqlAddr() {
        return sqlAddr;
    }

    public void setSqlAddr(String sqlAddr) {
        this.sqlAddr = sqlAddr;
    }
}

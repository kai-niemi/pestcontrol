package io.cockroachdb.pest.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaselineProperties {
    @NotNull
    private String serviceAddr;

    private String listenAddr;

    private String advertiseAddr;

    private String advertiseProxyAddr;

    private String sqlAddr;

    private String httpAddr;

    private List<String> certHosts = List.of();

    private final AtomicInteger id = new AtomicInteger();

    public BaselineProperties nextId() {
        id.incrementAndGet();
        return this;
    }

    public Integer getCurrentId() {
        return id.get();
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

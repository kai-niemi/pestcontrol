package io.cockroachdb.pest.model;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaselineSettings {
    public static String incrementPort(String base, int offset) {
        String[] parts = base.split(":");
        if (parts.length == 2) {
            return parts[0] + ":" + Integer.parseInt(parts[1]) + offset;
        }
        if (parts.length == 1) {
            return ":" + Integer.parseInt(parts[0]) + offset;
        }
        throw new IllegalArgumentException("Expected ip:port or :port" + base);
    }

    @NotNull
    private String serviceAddr;

    @NotNull
    private String listenAddr;

    @NotNull
    private String advertiseAddr;

    @NotNull
    private String advertiseProxyAddr;

    @NotNull
    private String sqlAddr;

    @NotNull
    private String httpAddr;

    private List<String> certHosts = List.of();

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

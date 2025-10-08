package io.cockroachdb.pest.web.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

import io.cockroachdb.pest.model.Cluster;

public class ClusterAuthenticationDetails extends WebAuthenticationDetails {
    private final String clusterId;

    private final Boolean useFileCredentials;

    private Cluster cluster;

    public ClusterAuthenticationDetails(HttpServletRequest request) {
        super(request);

        this.clusterId = request.getParameter("clusterId");
        this.useFileCredentials = Boolean.valueOf(request.getParameter("useFileCredentials"));
    }

    public String getClusterId() {
        return clusterId;
    }

    public Boolean getUseFileCredentials() {
        return useFileCredentials;
    }

    public Cluster getClusterProperties() {
        return cluster;
    }

    public void setClusterProperties(Cluster cluster) {
        this.cluster = cluster;
    }
}

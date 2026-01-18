package io.cockroachdb.pest.web.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

import io.cockroachdb.pest.domain.Cluster;

public class ClusterAuthenticationDetails extends WebAuthenticationDetails {
    private final String clusterId;

    private Cluster cluster;

    public ClusterAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.clusterId = request.getParameter("clusterId");
    }

    public String getClusterId() {
        return clusterId;
    }

    public Cluster getClusterProperties() {
        return cluster;
    }

    public void setClusterProperties(Cluster cluster) {
        this.cluster = cluster;
    }
}

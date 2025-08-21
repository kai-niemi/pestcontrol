package io.cockroachdb.pest.web.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

import io.cockroachdb.pest.model.ClusterSettings;

public class ClusterAuthenticationDetails extends WebAuthenticationDetails {
    private final String clusterId;

    private final Boolean useFileCredentials;

    private ClusterSettings clusterSettings;

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

    public ClusterSettings getClusterProperties() {
        return clusterSettings;
    }

    public void setClusterProperties(ClusterSettings clusterSettings) {
        this.clusterSettings = clusterSettings;
    }
}

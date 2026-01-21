package io.cockroachdb.pest.web.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

import io.cockroachdb.pest.web.model.ClusterModel;

public class ClusterAuthenticationDetails extends WebAuthenticationDetails {
    private final String clusterId;

    private ClusterModel clusterModel;

    public ClusterAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.clusterId = request.getParameter("clusterId");
    }

    public String getClusterId() {
        return clusterId;
    }

    public ClusterModel getClusterModel() {
        return clusterModel;
    }

    public void setClusterModel(ClusterModel clusterModel) {
        this.clusterModel = clusterModel;
    }
}

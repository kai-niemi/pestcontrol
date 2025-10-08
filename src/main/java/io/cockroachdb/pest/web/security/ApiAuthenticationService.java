package io.cockroachdb.pest.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;

@Component
public class ApiAuthenticationService {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    public Authentication getAuthentication(String clusterId) {
//            throw new BadCredentialsException("Missing API key header: " + AUTH_TOKEN_HEADER_NAME);
        if (!clusterManager.hasSessionToken(clusterId)) {
            Cluster cluster = applicationProperties.getClusterById(clusterId);
            clusterManager.login(cluster.getClusterId(),
                    cluster.getDataSourceProperties().getUsername(),
                    cluster.getDataSourceProperties().getPassword()
            );
        }
        return new ApiAuthenticationToken(clusterId, AuthorityUtils.NO_AUTHORITIES);
    }
}

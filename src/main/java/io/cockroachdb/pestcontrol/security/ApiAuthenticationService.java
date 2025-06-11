package io.cockroachdb.pestcontrol.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.cluster.ClusterManager;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;

@Component
public class ApiAuthenticationService {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    public Authentication getAuthentication(String clusterId) {
//            throw new BadCredentialsException("Missing API key header: " + AUTH_TOKEN_HEADER_NAME);
        if (!clusterManager.hasSessionToken(clusterId)) {
            ClusterProperties clusterProperties
                    = applicationProperties.getClusterPropertiesById(clusterId);
            clusterManager.login(clusterProperties.getClusterId(),
                    clusterProperties.getDataSourceProperties().getUsername(),
                    clusterProperties.getDataSourceProperties().getPassword()
            );
        }
        return new ApiAuthenticationToken(clusterId, AuthorityUtils.NO_AUTHORITIES);
    }
}

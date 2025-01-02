package io.cockroachdb.pc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.cockroachdb.pc.config.ApplicationProperties;
import io.cockroachdb.pc.schema.ClusterProperties;
import io.cockroachdb.pc.service.ClusterManager;

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

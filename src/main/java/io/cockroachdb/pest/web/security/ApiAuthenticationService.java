package io.cockroachdb.pest.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.model.ApplicationSettings;
import io.cockroachdb.pest.model.ClusterSettings;

@Component
public class ApiAuthenticationService {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationSettings applicationSettings;

    public Authentication getAuthentication(String clusterId) {
//            throw new BadCredentialsException("Missing API key header: " + AUTH_TOKEN_HEADER_NAME);
        if (!clusterManager.hasSessionToken(clusterId)) {
            ClusterSettings clusterSettings
                    = applicationSettings.getClusterPropertiesById(clusterId);
            clusterManager.login(clusterSettings.getClusterId(),
                    clusterSettings.getDataSourceProperties().getUsername(),
                    clusterSettings.getDataSourceProperties().getPassword()
            );
        }
        return new ApiAuthenticationToken(clusterId, AuthorityUtils.NO_AUTHORITIES);
    }
}

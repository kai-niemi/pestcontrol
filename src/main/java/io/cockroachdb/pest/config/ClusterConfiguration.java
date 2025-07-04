package io.cockroachdb.pest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import jakarta.annotation.PostConstruct;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.CredentialsHandler;
import io.cockroachdb.pest.model.ApplicationProperties;

@Configuration
public class ClusterConfiguration {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostConstruct
    public void init() {
        clusterManager.setCredentialsHandler(new CredentialsHandler() {
            @Override
            public Pair<String, String> getAuthenticationCredentials(String clusterId) {
                String username = applicationProperties.getClusterPropertiesById(clusterId)
                        .getDataSourceProperties()
                        .getUsername();
                String password = applicationProperties.getClusterPropertiesById(clusterId)
                        .getDataSourceProperties()
                        .getPassword();
                return Pair.of(username, password);
            }
        });
    }
}

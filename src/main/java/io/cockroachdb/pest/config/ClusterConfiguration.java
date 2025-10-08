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
                String username = applicationProperties.getDataSourceProperties(clusterId)
                        .getUsername();
                String password = applicationProperties.getDataSourceProperties(clusterId)
                        .getPassword();
                return Pair.of(username, password);
            }
        });
    }
}

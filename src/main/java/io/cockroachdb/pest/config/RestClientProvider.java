package io.cockroachdb.pest.config;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.model.ClusterProperties;

@FunctionalInterface
public interface RestClientProvider {
    RestClient matches(ClusterProperties clusterProperties);
}

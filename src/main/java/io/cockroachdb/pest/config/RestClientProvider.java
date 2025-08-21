package io.cockroachdb.pest.config;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.model.ClusterSettings;

@FunctionalInterface
public interface RestClientProvider {
    RestClient matches(ClusterSettings clusterSettings);
}

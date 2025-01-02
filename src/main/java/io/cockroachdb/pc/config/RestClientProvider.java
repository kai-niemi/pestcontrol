package io.cockroachdb.pc.config;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pc.schema.ClusterProperties;

@FunctionalInterface
public interface RestClientProvider {
    RestClient matches(ClusterProperties clusterProperties);
}

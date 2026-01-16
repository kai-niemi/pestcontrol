package io.cockroachdb.pest.config;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.domain.ClusterType;

@FunctionalInterface
public interface RestClientProvider {
    RestClient apply(ClusterType clusterType);
}

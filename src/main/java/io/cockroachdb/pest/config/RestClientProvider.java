package io.cockroachdb.pest.config;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.model.Cluster;

@FunctionalInterface
public interface RestClientProvider {
    RestClient matches(Cluster cluster);
}

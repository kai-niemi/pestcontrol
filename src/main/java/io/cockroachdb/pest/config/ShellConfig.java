package io.cockroachdb.pest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pest.shell.ClusterProvider;
import io.cockroachdb.pest.shell.NodeProvider;

@Configuration
public class ShellConfig {
    @Bean
    public NodeProvider agentProvider() {
        return new NodeProvider();
    }

    @Bean
    public ClusterProvider clusterProvider() {
        return new ClusterProvider();
    }
}

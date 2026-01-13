package io.cockroachdb.pest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pest.shell.support.ClusterCompletionProvider;
import io.cockroachdb.pest.shell.support.NodeProvider;

@Configuration
public class ShellConfiguration {
    @Bean
    public NodeProvider agentProvider() {
        return new NodeProvider();
    }

    @Bean
    public ClusterCompletionProvider clusterProvider() {
        return new ClusterCompletionProvider();
    }
}

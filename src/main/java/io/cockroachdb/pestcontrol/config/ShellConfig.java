package io.cockroachdb.pestcontrol.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pestcontrol.shell.ClusterProvider;
import io.cockroachdb.pestcontrol.shell.NodeProvider;

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

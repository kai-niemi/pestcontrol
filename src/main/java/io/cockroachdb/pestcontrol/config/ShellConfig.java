package io.cockroachdb.pestcontrol.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pestcontrol.shell.support.NodeProvider;

@Configuration
public class ShellConfig {
    @Bean
    public NodeProvider agentProvider() {
        return new NodeProvider();
    }
}

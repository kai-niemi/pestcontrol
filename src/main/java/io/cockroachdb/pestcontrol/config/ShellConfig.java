package io.cockroachdb.pestcontrol.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pestcontrol.shell.support.AgentProvider;

@Configuration
public class ShellConfig {
    @Bean
    public AgentProvider agentProvider() {
        return new AgentProvider();
    }
}

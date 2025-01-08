package io.cockroachdb.pc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pc.shell.support.AgentProvider;

@Configuration
public class ShellConfig {
    @Bean
    public AgentProvider agentProvider() {
        return new AgentProvider();
    }
}

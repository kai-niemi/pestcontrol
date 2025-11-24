package io.cockroachdb.pest.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Shell;
import org.springframework.shell.boot.NonInteractiveShellRunnerCustomizer;
import org.springframework.shell.context.ShellContext;

import io.cockroachdb.pest.shell.support.ClusterProvider;
import io.cockroachdb.pest.shell.support.NodeProvider;
import io.cockroachdb.pest.shell.support.OneOffShellRunner;

@Configuration
public class ShellConfiguration {
    @Bean
    public OneOffShellRunner oneOffShellRunner(Shell shell, ShellContext shellContext,
                                               ObjectProvider<NonInteractiveShellRunnerCustomizer> customizer) {
        OneOffShellRunner shellRunner = new OneOffShellRunner(shell, shellContext);
        customizer.orderedStream().forEach((c) -> c.customize(shellRunner));
        return shellRunner;
    }

    @Bean
    public NodeProvider agentProvider() {
        return new NodeProvider();
    }

    @Bean
    public ClusterProvider clusterProvider() {
        return new ClusterProvider();
    }
}

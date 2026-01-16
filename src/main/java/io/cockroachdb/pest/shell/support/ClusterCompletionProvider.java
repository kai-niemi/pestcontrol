package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.shell.core.command.completion.CompletionContext;
import org.springframework.shell.core.command.completion.CompletionProposal;
import org.springframework.shell.core.command.completion.CompletionProvider;

import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;

public class ClusterCompletionProvider implements CompletionProvider {
    private final ApplicationProperties applicationProperties;

    public ClusterCompletionProvider(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public List<CompletionProposal> apply(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (Cluster cluster : applicationProperties.getClusters()) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if ((cluster.getClusterName().startsWith(prefix)
                 || cluster.getClusterId().startsWith(prefix))) {
                result.add(new CompletionProposal(cluster.getClusterId())
                        .displayText(cluster.getClusterId())
                        .description(cluster.getClusterName()));
            }
        }

        result.add(new CompletionProposal("none")
                .displayText("none")
                .description("Clear selection"));

        return result;
    }
}

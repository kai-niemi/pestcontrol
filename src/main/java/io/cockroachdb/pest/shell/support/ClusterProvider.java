package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;

public class ClusterProvider implements ValueProvider {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
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

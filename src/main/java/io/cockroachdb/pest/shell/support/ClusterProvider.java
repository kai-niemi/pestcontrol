package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterTypes;

public class ClusterProvider implements ValueProvider {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (ClusterProperties clusterProperties : applicationProperties.getClusterProperties()) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if ((clusterProperties.getClusterName().startsWith(prefix)
                 || clusterProperties.getClusterId().startsWith(prefix))
                && ClusterTypes.isHosted(clusterProperties.getClusterType())) {
                result.add(new CompletionProposal(clusterProperties.getClusterId())
                        .displayText(clusterProperties.getClusterId())
                        .description(clusterProperties.getClusterName()));
            }
        }

        result.add(new CompletionProposal("none")
                .displayText("none")
                .description("Clear selection"));

        return result;
    }
}

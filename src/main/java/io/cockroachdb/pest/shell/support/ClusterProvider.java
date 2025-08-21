package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import io.cockroachdb.pest.model.ApplicationSettings;
import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterTypes;

public class ClusterProvider implements ValueProvider {
    @Autowired
    private ApplicationSettings applicationSettings;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (ClusterSettings clusterSettings : applicationSettings.getClusters()) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if ((clusterSettings.getClusterName().startsWith(prefix)
                 || clusterSettings.getClusterId().startsWith(prefix))
                && ClusterTypes.isHosted(clusterSettings.getClusterType())) {
                result.add(new CompletionProposal(clusterSettings.getClusterId())
                        .displayText(clusterSettings.getClusterId())
                        .description(clusterSettings.getClusterName()));
            }
        }

        result.add(new CompletionProposal("none")
                .displayText("none")
                .description("Clear selection"));

        return result;
    }
}

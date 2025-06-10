package io.cockroachdb.pestcontrol.shell;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;

public class ClusterProvider implements ValueProvider {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (ClusterProperties clusterProperties : applicationProperties.getClusters()) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if (clusterProperties.getClusterName().startsWith(prefix)
                || clusterProperties.getClusterId().startsWith(prefix)) {
                result.add(new CompletionProposal(clusterProperties.getClusterId())
                        .displayText(clusterProperties.getClusterId())
                        .description(clusterProperties.getClusterName() + " of type " + clusterProperties.getClusterType()));
            }
        }

        return result;
    }
}

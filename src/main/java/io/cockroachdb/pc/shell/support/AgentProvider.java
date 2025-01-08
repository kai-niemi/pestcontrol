package io.cockroachdb.pc.shell.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import io.cockroachdb.pc.model.AgentProperties;
import io.cockroachdb.pc.model.ApplicationProperties;

public class AgentProvider implements ValueProvider {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (AgentProperties agentProperties : applicationProperties.getAgents()) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if (agentProperties.getUrl().startsWith(prefix)) {
                result.add(new CompletionProposal(agentProperties.getUrl()).description(agentProperties.getName()));
            }
        }

        return result;
    }
}

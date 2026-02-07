package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.shell.core.command.completion.CompletionContext;
import org.springframework.shell.core.command.completion.CompletionProposal;
import org.springframework.shell.core.command.completion.CompletionProvider;

public class NodeRangeProvider implements CompletionProvider {
    private final int numNodes;

    public NodeRangeProvider(int numNodes) {
        this.numNodes = numNodes;
    }

    @Override
    public List<CompletionProposal> apply(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        result.add(new CompletionProposal(  "all").description("All nodes"));

        IntStream.rangeClosed(1, numNodes).forEach(value ->
                result.add(new CompletionProposal(value + "")
                        .description("Node #" + value)));

        return result;
    }
}

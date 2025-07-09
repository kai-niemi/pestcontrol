package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

public class NodeProvider implements ValueProvider {
    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        IntStream.rangeClosed(1, 32).forEach(value ->
                result.add(new CompletionProposal(value + "")
                        .description("Node #" + value)));

        return result;
    }
}

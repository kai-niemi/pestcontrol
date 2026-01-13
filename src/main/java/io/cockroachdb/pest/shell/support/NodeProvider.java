package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.shell.core.command.completion.CompletionContext;
import org.springframework.shell.core.command.completion.CompletionProposal;
import org.springframework.shell.core.command.completion.CompletionProvider;

public class NodeProvider implements CompletionProvider {
    @Override
    public List<CompletionProposal> apply(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        IntStream.rangeClosed(1, 32).forEach(value ->
                result.add(new CompletionProposal(value + "")
                        .description("Node #" + value)));

        return result;
    }
}

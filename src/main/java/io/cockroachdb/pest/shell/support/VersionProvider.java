package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.shell.core.command.completion.CompletionContext;
import org.springframework.shell.core.command.completion.CompletionProposal;
import org.springframework.shell.core.command.completion.CompletionProvider;

public class VersionProvider implements CompletionProvider {
    private final String prefix;

    public VersionProvider(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public List<CompletionProposal> apply(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        IntStream.range(0, 1).forEach(value -> {
            result.add(new CompletionProposal(prefix + "=" + "v26.1." + value));
        });
        IntStream.range(0, 4).forEach(value -> {
            result.add(new CompletionProposal(prefix + "=" + "v25.4." + value));
        });
        IntStream.range(0, 11).forEach(value -> {
            result.add(new CompletionProposal(prefix + "=" + "v25.2." + value));
        });
        IntStream.range(0, 26).forEach(value -> {
            result.add(new CompletionProposal(prefix + "=" + "v24.3." + value));
        });
        IntStream.range(6, 26).forEach(value -> {
            result.add(new CompletionProposal(prefix + "=" + "v24.1." + value));
        });
        IntStream.range(7, 29).forEach(value -> {
            result.add(new CompletionProposal(prefix + "=" + "v23.2." + value));
        });

        return result;
    }
}

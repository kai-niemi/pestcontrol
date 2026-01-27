package io.cockroachdb.pest.shell.support;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.shell.AdminConfigCommands;

@Component
public class ShellPromptProvider implements PromptProvider {
    @Autowired
    private AdminConfigCommands adminConfigCommands;

    @Override
    public AttributedString getPrompt() {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("pestcontrol", AttributedStyle.DEFAULT
                .foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT));

        if (!adminConfigCommands.ifClusterSelected().isAvailable()) {
            sb.append(" $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        } else {
            Cluster cluster = adminConfigCommands.selectedCluster();
            sb.append(" (", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
            sb.append(cluster.getClusterId(), AttributedStyle.DEFAULT
                    .backgroundDefault()
                    .foreground(AttributedStyle.WHITE)
                    .faintDefault());
            sb.append(") $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        }

        return sb.toAttributedString();
    }
}

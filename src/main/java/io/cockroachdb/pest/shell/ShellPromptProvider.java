package io.cockroachdb.pest.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.ClusterProperties;

@Component
public class ShellPromptProvider implements PromptProvider {
    @Autowired
    private ClusterCommands clusterCommands;

    @Override
    public AttributedString getPrompt() {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("pest", AttributedStyle.DEFAULT
                .foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT));

        if (!clusterCommands.ifClusterSelected().isAvailable()) {
            sb.append(" $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        } else {
            ClusterProperties clusterProperties = clusterCommands.getClusterProperties(null);
            sb.append(" cluster:(", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
            sb.append(clusterProperties.getClusterId(), AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.RED | AttributedStyle.BRIGHT)
                    .faintOff());
            sb.append(") $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        }

        return sb.toAttributedString();
    }
}

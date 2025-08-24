package io.cockroachdb.pest.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.ClusterSettings;

@Component
public class ShellPromptProvider implements PromptProvider {
    @Autowired
    private SetupCommands setupCommands;

    @Override
    public AttributedString getPrompt() {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("pest", AttributedStyle.DEFAULT
                .foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT));

        if (!setupCommands.ifClusterSelected().isAvailable()) {
            sb.append(" $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        } else {
            ClusterSettings clusterSettings = setupCommands.getClusterSettings();
            sb.append(" cluster:(", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
            sb.append(clusterSettings.getClusterId(), AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.RED | AttributedStyle.BRIGHT)
                    .faintOff());
            sb.append(") $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        }

        return sb.toAttributedString();
    }
}

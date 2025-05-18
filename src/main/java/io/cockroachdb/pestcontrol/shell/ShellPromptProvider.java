package io.cockroachdb.pestcontrol.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class ShellPromptProvider implements PromptProvider {
    @Autowired
    private Environment environment;

    @Override
    public AttributedString getPrompt() {
        String profiles = String.join(",", environment.getActiveProfiles());
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("pestcontrol (", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        sb.append(profiles, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).faintOff());
        sb.append(")$ ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        return sb.toAttributedString();
    }
}

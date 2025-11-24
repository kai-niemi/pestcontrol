package io.cockroachdb.pest.shell.support;

import java.util.stream.IntStream;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class AnsiConsole {
    private final Terminal terminal;

    public AnsiConsole(@Autowired @Lazy Terminal terminal) {
        Assert.notNull(terminal, "terminal is null");
        this.terminal = terminal;
    }

    public AnsiConsole cyan(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_CYAN, format, args);
    }

    public AnsiConsole red(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_RED, format, args);
    }

    public AnsiConsole green(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_GREEN, format, args);
    }

    public AnsiConsole blue(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_BLUE, format, args);
    }

    public AnsiConsole yellow(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_YELLOW, format, args);
    }

    public AnsiConsole magenta(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_MAGENTA, format, args);
    }

    public AnsiConsole printf(AnsiColor color, String text, Object... args) {
        terminal.writer().printf(AnsiOutput.toString(color, text.formatted(args), AnsiColor.DEFAULT));
        terminal.writer().flush();
        return this;
    }

    public AnsiConsole nl() {
        return nl(1);
    }

    public AnsiConsole nl(int c) {
        IntStream.rangeClosed(1, c).forEach(value -> terminal.writer().println());
        terminal.writer().flush();
        return this;
    }
}

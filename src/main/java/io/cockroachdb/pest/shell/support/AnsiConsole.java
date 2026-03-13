package io.cockroachdb.pest.shell.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.util.Assert;

public class AnsiConsole {
    public static AnsiConsole instance() {
        try {
            return new AnsiConsole(TerminalBuilder.terminal());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Lock lock = new ReentrantLock();

    private final Terminal terminal;

    public AnsiConsole(Terminal terminal) {
        Assert.notNull(terminal, "terminal is null");
        this.terminal = terminal;
    }

    public AnsiConsole header(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_MAGENTA, format, args);
    }

    public AnsiConsole debug(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_BLUE, format, args);
    }

    public AnsiConsole info(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_GREEN, format, args);
    }

    public AnsiConsole warn(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_YELLOW, format, args);
    }

    public AnsiConsole error(String format, Object... args) {
        return printf(AnsiColor.BRIGHT_RED, format, args);
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

    private AnsiConsole printf(AnsiColor color, String format, Object... args) {
        return println(color, String.format(Locale.US, format, args));
    }

    private AnsiConsole println(AnsiColor color, String text) {
        try {
            lock.lock();
            terminal.writer().print(AnsiOutput.toString(color, text, AnsiColor.DEFAULT));
            terminal.writer().println();
            terminal.writer().flush();
            return this;
        } finally {
            lock.unlock();
        }
    }
}

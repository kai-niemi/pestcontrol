package io.cockroachdb.pest.shell.support;

import java.io.PrintWriter;
import java.util.stream.IntStream;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class AnsiConsole {
    private final PrintWriter pw;

    public AnsiConsole(PrintWriter pw) {
        this.pw = pw;
    }

    public AnsiConsole cyan(String format, Object... args) {
        return printfn(AnsiColor.BRIGHT_CYAN, format, args);
    }

    public AnsiConsole red(String format, Object... args) {
        return printfn(AnsiColor.BRIGHT_RED, format, args);
    }

    public AnsiConsole green(String format, Object... args) {
        return printfn(AnsiColor.BRIGHT_GREEN, format, args);
    }

    public AnsiConsole blue(String format, Object... args) {
        return printfn(AnsiColor.BRIGHT_BLUE, format, args);
    }

    public AnsiConsole yellow(String format, Object... args) {
        return printfn(AnsiColor.BRIGHT_YELLOW, format, args);
    }

    public AnsiConsole magenta(String format, Object... args) {
        return printfn(AnsiColor.BRIGHT_MAGENTA, format, args);
    }

    public AnsiConsole printfn(AnsiColor color, String text, Object... args) {
        pw.printf(AnsiOutput.toString(color, text.formatted(args), AnsiColor.DEFAULT));
        pw.println();
        pw.flush();
        return this;
    }

    public AnsiConsole nl() {
        return nl(1);
    }

    public AnsiConsole nl(int c) {
        IntStream.rangeClosed(1, c).forEach(value -> pw.println());
        pw.flush();
        return this;
    }
}

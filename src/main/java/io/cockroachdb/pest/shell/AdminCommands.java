package io.cockroachdb.pest.shell;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;

import io.cockroachdb.pest.config.DataSourceConfiguration;
import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.shell.support.AnsiConsole;

@Component
public class AdminCommands {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Command(description = "Toggle dry run for local commands",
            name = {"admin", "toggle", "dry-run"},
            group = CommandGroups.ADMIN_COMMANDS)
    public void toggleDryRun(CommandContext commandContext) {
        AnsiConsole console = new AnsiConsole(commandContext.outputWriter());
        applicationProperties.setDryRunLocalCommands(!applicationProperties.isDryRunLocalCommands());
        boolean enabled = applicationProperties.isDryRunLocalCommands();
        console.green("Dry run mode %s", enabled ? "ENABLED" : "DISABLED");
    }

    @Command(description = "Toggle SQL trace logging (verbose)",
            name = {"admin", "toggle", "sql-trace"},
            group = CommandGroups.ADMIN_COMMANDS)
    public void toggleSqlTraceLogging(CommandContext commandContext) {
        AnsiConsole console = new AnsiConsole(commandContext.outputWriter());
        boolean enabled = toggleLogLevel(DataSourceConfiguration.SQL_TRACE_LOGGER);
        console.green("SQL Trace Logging %s", enabled ? "ENABLED" : "DISABLED");
    }

    private boolean toggleLogLevel(String name) {
        ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory
                .getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(name);
        if (logger.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.DEBUG)) {
            logger.setLevel(Level.TRACE);
            return true;
        } else {
            logger.setLevel(Level.DEBUG);
            return false;
        }
    }

    @Command(description = "Print system information",
            name = {"admin", "info"},
            group = CommandGroups.ADMIN_COMMANDS)
    public void systemInfo(CommandContext commandContext) {
        AnsiConsole console = new AnsiConsole(commandContext.outputWriter());

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        console.green(">> OS");
        console.yellow(" Arch: %s | OS: %s | Version: %s".formatted(os.getArch(), os.getName(), os.getVersion()));
        console.yellow(" Available processors: %d".formatted(os.getAvailableProcessors()));
        console.yellow(" Load avg: %f".formatted(os.getSystemLoadAverage()));

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        console.green(">> Runtime");
        console.yellow(" Uptime: %s".formatted(r.getUptime()));
        console.yellow(
                " VM name: %s | Vendor: %s | Version: %s".formatted(r.getVmName(), r.getVmVendor(), r.getVmVersion()));

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        console.green(">> Runtime");
        console.yellow(" CPU time: %d".formatted(t.getCurrentThreadCpuTime()));
        console.yellow(" User time: %d".formatted(t.getCurrentThreadUserTime()));
        console.yellow(" Peak threads: %d".formatted(t.getPeakThreadCount()));
        console.yellow(" Thread #: %d".formatted(t.getThreadCount()));
        console.yellow(" Total started threads: %d".formatted(t.getTotalStartedThreadCount()));

        Arrays.stream(t.getAllThreadIds()).sequential().forEach(value -> {
            console.yellow(" Thread (%d): %s %s".formatted(value,
                    t.getThreadInfo(value).getThreadName(),
                    t.getThreadInfo(value).getThreadState().toString()
            ));
        });

        MemoryMXBean m = ManagementFactory.getMemoryMXBean();
        console.green(">> Memory");
        console.yellow(" Heap: %s".formatted(m.getHeapMemoryUsage().toString()));
        console.yellow(" Non-heap: %s".formatted(m.getNonHeapMemoryUsage().toString()));
    }
}

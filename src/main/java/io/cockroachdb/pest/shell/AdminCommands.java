package io.cockroachdb.pest.shell;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Level;

import io.cockroachdb.pest.config.DataSourceConfiguration;
import io.cockroachdb.pest.model.ApplicationProperties;

@Component
public class AdminCommands {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Command(description = "Exit the shell", name = {"quit", "exit", "q"},
            group = Constants.ADMIN_COMMANDS)
    public void quit() {
        logger.info("Quitting");
        SpringApplication.exit(applicationContext, () -> 0);
        System.exit(0);
    }

    @Command(description = "Toggle dry run for local commands", name = {
            "dry-run", "dr"}, group = Constants.ADMIN_COMMANDS)
    public void toggleDryRun() {
        applicationProperties.setDryRunLocalCommands(!applicationProperties.isDryRunLocalCommands());
        boolean enabled = applicationProperties.isDryRunLocalCommands();
        logger.info("Dry run mode is {}", enabled ? "ENABLED" : "DISABLED");
    }

    @Command(description = "Toggle SQL trace logging (verbose)", name = {
            "sql-trace", "t"}, group = Constants.ADMIN_COMMANDS)
    public void toggleSqlTraceLogging() {
        boolean enabled = toggleLogLevel(DataSourceConfiguration.SQL_TRACE_LOGGER);
        logger.info("SQL Trace Logging {}", enabled ? "ENABLED" : "DISABLED");
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

    @Command(description = "Print system information", name = {"system-info", "si"}, group = Constants.ADMIN_COMMANDS)
    public void systemInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        logger.info(">> OS");
        logger.info(" Arch: %s | OS: %s | Version: %s".formatted(os.getArch(), os.getName(), os.getVersion()));
        logger.info(" Available processors: %d".formatted(os.getAvailableProcessors()));
        logger.info(" Load avg: %f".formatted(os.getSystemLoadAverage()));

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        logger.info(">> Runtime");
        logger.info(" Uptime: %s".formatted(r.getUptime()));
        logger.info(
                " VM name: %s | Vendor: %s | Version: %s".formatted(r.getVmName(), r.getVmVendor(), r.getVmVersion()));

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        logger.info(">> Runtime");
        logger.info(" CPU time: %d".formatted(t.getCurrentThreadCpuTime()));
        logger.info(" User time: %d".formatted(t.getCurrentThreadUserTime()));
        logger.info(" Peak threads: %d".formatted(t.getPeakThreadCount()));
        logger.info(" Thread #: %d".formatted(t.getThreadCount()));
        logger.info(" Total started threads: %d".formatted(t.getTotalStartedThreadCount()));

        Arrays.stream(t.getAllThreadIds()).sequential().forEach(value -> {
            logger.info(" Thread (%d): %s %s".formatted(value,
                    t.getThreadInfo(value).getThreadName(),
                    t.getThreadInfo(value).getThreadState().toString()
            ));
        });

        MemoryMXBean m = ManagementFactory.getMemoryMXBean();
        logger.info(">> Memory");
        logger.info(" Heap: %s".formatted(m.getHeapMemoryUsage().toString()));
        logger.info(" Non-heap: %s".formatted(m.getNonHeapMemoryUsage().toString()));
    }
}

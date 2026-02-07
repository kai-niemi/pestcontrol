package io.cockroachdb.pest.cluster.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.NetworkAddress;
import io.cockroachdb.pest.model.Node;

public class CommandBuilder {
    public static final String OPERATOR_SCRIPT = "./pest-op";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<String> commands = new ArrayList<>();

    private Path baseDir;

    private CommandBuilder() {
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public CommandBuilder withBaseDir(Path baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public CommandBuilder withCommand(String command) {
        withFlags(List.of(OPERATOR_SCRIPT, command));
        return this;
    }

    public CommandBuilder withFlags(String... args) {
        withFlags(List.of(args));
        return this;
    }

    public CommandBuilder withFlags(List<String> args) {
        this.commands.addAll(args);
        return this;
    }

    public CommandBuilder withServerNetworkingFlags(Cluster cluster, int nodeId) {
        Node node = cluster.getNodeById(nodeId);

        if (StringUtils.hasLength(node.getAdvertiseProxyAddr()) && cluster.isToxiProxyEnabled()) {
            commands.add("--advertise-addr=" + node.getAdvertiseProxyAddr());
        } else if (StringUtils.hasLength(node.getAdvertiseAddr())) {
            commands.add("--advertise-addr=" + node.getAdvertiseAddr());
        }

        if (StringUtils.hasLength(node.getListenAddr())) {
            commands.add("--listen-addr=" + node.getListenAddr());
        }

        if (StringUtils.hasLength(node.getSqlAddr())) {
            commands.add("--sql-addr=" + node.getSqlAddr());
        }

        if (StringUtils.hasLength(node.getHttpAddr())) {
            commands.add("--http-addr=" + node.getHttpAddr());
        }

        if (cluster.isSecure()) {
            commands.add("--secure");
        }
        return this;
    }

    public CommandBuilder withClientNetworkingFlags(Cluster cluster, int nodeId) {
        Node node = cluster.getNodeById(nodeId);

        commands.add("--rpc-addr=" + node.getJoinAddress());

        if (NetworkAddress.from(node.getSqlAddr()).getAddress().isEmpty()) {
            commands.add("--sql-addr=" + NetworkAddress.getLoopbackHostname() + node.getSqlAddr());
        } else {
            commands.add("--sql-addr=" + node.getSqlAddr());
        }

        if (cluster.isSecure()) {
            commands.add("--secure");
        }
        return this;
    }

    public List<String> executeAndCollect() throws IOException {
        Assert.notNull(baseDir, "baseDir is null");

        logger.info("Starting process: %s".formatted(String.join("\n\t", commands)));

        Instant start = Instant.now();

        Process process = new ProcessBuilder()
                .command(commands)
                .directory(baseDir.toFile())
                .redirectErrorStream(true)
                .start();

        try {
            logger.info("Process (pid %s) started: %s"
                    .formatted(process.pid(), process.info().commandLine().orElse("")));

            List<String> output = new ArrayList<>();
            try (BufferedReader reader = process.inputReader()) {
                reader.lines().forEach(s -> {
                    logger.info(s);
                    output.add(s);
                });
            }

            logger.info("Process (pid %s) waiting for termination: %s"
                    .formatted(process.pid(), process.info().commandLine().orElse("")));

            int code = process.waitFor();

            logger.info("Process (pid %s) terminated with exit code %d (%s)"
                    .formatted(process.pid(), code,
                            Duration.between(start, Instant.now())));


            return output;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            logger.warn("Process (pid %s) wait timeout  - forcibly closed".formatted(process.pid()));
            throw new IOException("Timeout waiting for process completion", e);
        }
    }

    public String execute() throws IOException {
        Assert.notNull(baseDir, "baseDir is null");

        logger.info("Starting process: %s".formatted(String.join("\n\t", commands)));

        Instant start = Instant.now();

        Process process = new ProcessBuilder()
                .command(commands)
                .directory(baseDir.toFile())
                .inheritIO()
                .start();

        try {
            logger.info("Process (pid %s) waiting for termination: %s"
                    .formatted(process.pid(), process.info().commandLine().orElse("")));

            int code = process.waitFor();

            logger.info("Process (pid %s) terminated with exit code %d (%s)"
                    .formatted(process.pid(), code,
                            Duration.between(start, Instant.now())));


            return "";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            logger.warn("Process (pid %s) wait timeout  - forcibly closed".formatted(process.pid()));
            throw new IOException("Timeout waiting for process completion", e);
        }
    }
}

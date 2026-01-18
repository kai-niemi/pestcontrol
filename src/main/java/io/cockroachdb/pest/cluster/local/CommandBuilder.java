package io.cockroachdb.pest.cluster.local;

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

import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.NetworkAddress;

public class CommandBuilder {
    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String OPERATOR_SCRIPT = "./pest-op";

    private final List<String> commands = new ArrayList<>();

    private Path baseDir;

    private boolean toxiProxy;

    private CommandBuilder() {
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

    public CommandBuilder withToxiProxy(boolean toxiProxy) {
        this.toxiProxy = toxiProxy;
        return this;
    }

    public CommandBuilder withFlags(List<String> args) {
        this.commands.addAll(args);
        return this;
    }

    public CommandBuilder withServerNetworkingFlags(Cluster cluster, int nodeId) {
        Cluster.Node node = cluster.getNodeById(nodeId);

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
        Cluster.Node node = cluster.getNodeById(nodeId);

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
            logger.info("Process (pid %s) started - waiting for termination: %s"
                    .formatted(process.pid(),
                            process.info().commandLine().orElse("")));

            int code = process.waitFor();

            logger.info("Process (pid %s) terminated with exit code %d (%s)"
                    .formatted(process.pid(), code,
                            Duration.between(start, Instant.now())));

            return "" + code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            process.destroyForcibly();
            process.onExit().join();

            logger.warn("Process (pid %s) wait timeout  - forcibly closed".formatted(process.pid()));

            throw new IOException("Timeout waiting for process completion", e);
        }
    }
}

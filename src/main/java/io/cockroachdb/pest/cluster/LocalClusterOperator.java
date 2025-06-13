package io.cockroachdb.pest.cluster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.NodeProperties;
import io.cockroachdb.pest.util.IoUtils;

@Component
public class LocalClusterOperator implements ClusterOperator {
    public static final int PROCESS_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure).contains(clusterType);
    }

    @Override
    public String install(ClusterProperties clusterProperties, Integer nodeId) {
        List<String> args = List.of("./cluster-admin", "agent-install",
                "--version=" + clusterProperties.getVersion()
        );
        return executeCommand(args);
    }

    @Override
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        Map<Locality, List<String>> hosts = new TreeMap<>();

        clusterProperties.getNodes()
                .forEach(np -> {
                    Locality locality = Locality.fromTiers(np.getLocality());
                    hosts.computeIfAbsent(locality, x -> new ArrayList<>())
                            .add(np.getListenAddr());
                });

        Collection<String> joinHosts = Locality.distributeJoinHosts(hosts);

        List<String> args = List.of("./cluster-admin", "agent-start",
                "--name=n" + nodeId,
                "--locality=" + nodeProperties.getLocality(),
                "--listen-addr=" + nodeProperties.getListenAddr(),
                "--advertise-addr=" + nodeProperties.getAdvertiseAddr(),
                "--sql-addr=" + nodeProperties.getSqlAddr(),
                "--http-addr=" + nodeProperties.getHttpAddr(),
                "--join=" + String.join(",", joinHosts)
        );

        return executeCommand(args);
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "agent-stop",
                "--sql-addr=" + nodeProperties.getSqlAddr()
        );

        return executeCommand(args);
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "agent-init",
                "--listen-addr=" + nodeProperties.getListenAddr(),
                "--sql-addr=" + nodeProperties.getSqlAddr()
        );

        return executeCommand(args);
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        return stopNode(clusterProperties, nodeId);
    }

    @Override
    public String disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "disrupt", nodeProperties.getSqlAddr());

        return executeCommand(args);
    }

    @Override
    public String recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "recover", nodeProperties.getSqlAddr());

        return executeCommand(args);
    }

    @Override
    public String disruptNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

    private String executeCommand(List<String> args) {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeCommand(args, barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
        return StreamUtils.copyToString(barr, Charset.defaultCharset());
    }

    private int executeCommand(List<String> commands, ByteArrayOutputStream barr) {
        Instant start = Instant.now();

        try {
            logger.debug("Starting process: %s".formatted(commands));

            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(Paths.get(applicationProperties.getScriptPath()).toFile())
                    .start();

            logger.debug("Started process: %s".formatted(process.info()));

            process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream()) {
                IoUtils.copy(inputStream, barr);
                IoUtils.copy(errorStream, barr);
            }

            int code = process.exitValue();

            logger.debug("Process finished in %s with exit code %d: %s"
                    .formatted(Duration.between(start, Instant.now()), code, process.info()));

            return code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Timeout waiting for process completion", e);
        } catch (IOException e) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), e);
        }
    }
}

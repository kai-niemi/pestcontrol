package io.cockroachdb.pestcontrol.operator;

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

import io.cockroachdb.pestcontrol.manager.CommandException;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.Locality;
import io.cockroachdb.pestcontrol.model.NodeProperties;
import io.cockroachdb.pestcontrol.util.IoUtils;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure).contains(clusterType);
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

        Collection<String> joinHosts = Locality.resolveJoinHosts(hosts);

        List<String> args = List.of("./cluster-admin", "agent-start",
                "--locality=" + nodeProperties.getLocality(),
                "--listen-addr="+ nodeProperties.getListenAddr(),
                "--advertise-addr=" + nodeProperties.getAdvertiseAddr(),
                "--sql-addr=" + nodeProperties.getSqlAddr(),
                "--http-addr=" + nodeProperties.getHttpAddr(),
                "--join=" + String.join(",", joinHosts)
        );

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(args, barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }

        return barr.toString();
    }

    @Override
    public void stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void killNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(List.of("./cluster-admin", "disrupt", nodeProperties.getSqlAddr()), barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(List.of("./cluster-admin", "recover", nodeProperties.getSqlAddr()), barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
    }

    @Override
    public void disruptNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recoverNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

    private int executeProcess(List<String> commands, ByteArrayOutputStream barr) {
        Instant start = Instant.now();

        try {
            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(Paths.get(applicationProperties.getScriptPath()).toFile())
                    .start();

            logger.debug("Started process: %s".formatted(process.info()));

            process.waitFor(30, TimeUnit.SECONDS);

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

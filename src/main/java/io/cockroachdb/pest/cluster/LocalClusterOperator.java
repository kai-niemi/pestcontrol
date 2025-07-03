package io.cockroachdb.pest.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.NodeProperties;
import io.cockroachdb.pest.util.Networking;
import static io.cockroachdb.pest.util.ProcessUtils.executeCommand;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public boolean supports(ClusterType clusterType) {
        return false;
    }

    private void addNetworkingFlags(NodeProperties nodeProperties, List<String> args) {
        if (StringUtils.hasLength(nodeProperties.getListenAddr())) {
            args.add("--listen-addr=" + nodeProperties.getListenAddr());
        }

        if (StringUtils.hasLength(nodeProperties.getAdvertiseAddr())) {
            args.add("--advertise-addr=" + nodeProperties.getAdvertiseAddr());
        } else {
            if (StringUtils.hasLength(nodeProperties.getListenAddr())) {
                args.add("--advertise-addr=" + nodeProperties.getListenAddr());
            } else {
                args.add("--advertise-addr=" + Networking.getCanonicalHostName() + ":26257");
            }
        }

        if (StringUtils.hasLength(nodeProperties.getSqlAddr())) {
            args.add("--sql-addr=" + nodeProperties.getSqlAddr());
        }

        if (StringUtils.hasLength(nodeProperties.getHttpAddr())) {
            args.add("--http-addr=" + nodeProperties.getHttpAddr());
        }

        if (nodeProperties.isSecure()) {
            args.add("--secure");
        }
    }

    @Override
    public Map<Integer, List<Path>> certs(ClusterProperties cluster, List<Integer> nodeIds) {
        // First create CA cert and key pairs
        executeCommand(applicationProperties.getScriptDirectory(),
                List.of("./pest-control", "cert"));

        Map<Integer, List<Path>> keyFiles = new HashMap<>();

        // Then create node cert and key pairs
        cluster.getNodes().forEach(nodeProperties -> {
            List<Path> expectedFiles = new ArrayList<>();
            expectedFiles.add(applicationProperties.getCertsDirectory()
                    .resolve(nodeProperties.getName()).resolve("node.crt"));
            expectedFiles.add(applicationProperties.getCertsDirectory()
                    .resolve(nodeProperties.getName()).resolve("node.key"));

            List<String> command = new ArrayList<>(List.of("./pest-control", "node-cert"));
            command.add("--name=" + nodeProperties.getName());
            command.addAll(nodeProperties.getCertHosts());

            executeCommand(applicationProperties.getScriptDirectory(), command);

            expectedFiles.forEach(path -> {
                if (!Files.isReadable(path)) {
                    throw new UncheckedIOException(
                            new IOException("Expected node key file not found or readable: " + path));
                }
            });

            keyFiles.put(nodeProperties.getId(), expectedFiles);
        });

        return keyFiles;
    }

    @Override
    public String install(ClusterProperties clusterProperties, Integer nodeId) {
        List<String> args = List.of("./pest-control", "install",
                "--version=" + clusterProperties.getVersion()
        );
        return executeCommand(applicationProperties.getScriptDirectory(), args).getFirst();
    }

    @Override
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        Map<Locality, List<String>> hosts = new TreeMap<>();

        clusterProperties.getNodes()
                .forEach(np -> {
                    Locality locality = Locality.fromTiers(np.getLocality());
                    hosts.computeIfAbsent(locality, x -> new ArrayList<>())
                            .add(Objects.requireNonNull(np.getAdvertiseAddr()));
                });

        List<String> args = new ArrayList<>(List.of("./pest-control", "start"));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + nodeProperties.getLocality());

        addNetworkingFlags(nodeProperties, args);

        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(hosts)));

        return executeCommand(applicationProperties.getScriptDirectory(), args).getFirst();
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of("./pest-control", "stop"));
        addNetworkingFlags(nodeProperties, args);

        return executeCommand(applicationProperties.getScriptDirectory(), args).getFirst();
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of("./pest-control", "init"));
        addNetworkingFlags(nodeProperties, args);

        return executeCommand(applicationProperties.getScriptDirectory(), args).getFirst();
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of("./pest-control", "kill"));
        addNetworkingFlags(nodeProperties, args);

        return executeCommand(applicationProperties.getScriptDirectory(), args).getFirst();
    }

    @Override
    public String sqlNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of("./pest-control", "sql"));
        addNetworkingFlags(nodeProperties, args);

        return executeCommand(applicationProperties.getScriptDirectory(), args).getFirst();
    }

    @Override
    public String disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String disruptNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

}

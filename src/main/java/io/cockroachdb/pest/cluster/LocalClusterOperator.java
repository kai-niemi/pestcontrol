package io.cockroachdb.pest.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
    private static final String OPERATOR_SCRIPT = "./pop";

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public boolean supports(ClusterType clusterType) {
        return false;
    }

    private void addNetworkingFlags(ClusterProperties clusterProperties,
                                    NodeProperties nodeProperties,
                                    List<String> args) {
        if (StringUtils.hasLength(nodeProperties.getListenAddr())) {
            args.add("--listen-addr=" + nodeProperties.getListenAddr());
        }

        if (applicationProperties.getToxiProxy().isEnabled()) {
            args.add("--advertise-addr=" +
                     Objects.requireNonNull(nodeProperties.getAdvertiseProxyAddr()));
        } else {
            if (StringUtils.hasLength(nodeProperties.getAdvertiseAddr())) {
                args.add("--advertise-addr=" + nodeProperties.getAdvertiseAddr());
            } else {
                if (StringUtils.hasLength(nodeProperties.getListenAddr())) {
                    args.add("--advertise-addr=" + nodeProperties.getListenAddr());
                } else {
                    args.add("--advertise-addr=" + Networking.getCanonicalHostName() + ":26257");
                }
            }
        }

        if (StringUtils.hasLength(nodeProperties.getSqlAddr())) {
            args.add("--sql-addr=" + nodeProperties.getSqlAddr());
        }

        if (StringUtils.hasLength(nodeProperties.getHttpAddr())) {
            args.add("--http-addr=" + nodeProperties.getHttpAddr());
        }

        if (clusterProperties.isSecure()) {
            args.add("--secure");
        }
    }

    @Override
    public String certs(ClusterProperties clusterProperties, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        // First create CA cert and key pairs
        executeCommand(applicationProperties.getBaseDirPath(), List.of(OPERATOR_SCRIPT, "cert"));

        // Then create node cert and key pairs
        clusterProperties.getNodes().forEach(nodeProperties -> {
            List<Path> expectedFiles = new ArrayList<>();
            expectedFiles.add(applicationProperties.getCertsDirPath()
                    .resolve(nodeProperties.getName()).resolve("node.crt"));
            expectedFiles.add(applicationProperties.getCertsDirPath()
                    .resolve(nodeProperties.getName()).resolve("node.key"));

            List<String> command = new ArrayList<>(List.of(OPERATOR_SCRIPT, "node-cert"));
            command.add("--name=" + nodeProperties.getName());
            command.addAll(nodeProperties.getCertHosts());

            executeCommand(applicationProperties.getBaseDirPath(), command);

            expectedFiles.forEach(path -> {
                if (!Files.isReadable(path)) {
                    throw new UncheckedIOException(
                            new IOException("Expected node key file not found or readable: " + path));
                }
            });

            keyFiles.put(nodeProperties.getId(), expectedFiles);
        });

        return "";
    }

    @Override
    public String install(ClusterProperties clusterProperties, Integer nodeId) {
        List<String> args = List.of(OPERATOR_SCRIPT, "install",
                "--version=" + clusterProperties.getVersion()
        );
        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "init"));
        addNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String wipe(ClusterProperties clusterProperties, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "wipe"));
        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
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

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start"));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + nodeProperties.getLocality());

        addNetworkingFlags(clusterProperties, nodeProperties, args);

        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(hosts)));

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop"));
        addNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "kill"));
        addNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String sqlNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "sql"));
        addNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        return killNode(clusterProperties, nodeId);
    }

    @Override
    public String recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        return startNode(clusterProperties, nodeId);
    }

    @Override
    public String disruptLocality(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverLocality(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startProxyServer(ClusterProperties cluster) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-proxy"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiProxy().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiProxy().getPort());
        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String startProxyClient(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-proxy-cli"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiProxy().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiProxy().getPort());
        args.add("--listen_addr=" + nodeProperties.getAdvertiseAddr());
        args.add("--upstream_addr=" + nodeProperties.getListenAddr());
        args.add("--name=" + nodeProperties.getName());

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String stopProxyServer(ClusterProperties cluster) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-proxy"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiProxy().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiProxy().getPort());
        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String startLoadBalancer(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-lb"));
        args.add("--advertise-addr=" + nodeProperties.getAdvertiseAddr());

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String stopLoadBalancer(ClusterProperties cluster) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-lb"));

        return executeCommand(applicationProperties.getBaseDirPath(), args).getFirst();
    }
}

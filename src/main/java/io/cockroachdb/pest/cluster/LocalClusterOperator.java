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

import io.cockroachdb.pest.model.ApplicationSettings;
import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.NodeSettings;
import io.cockroachdb.pest.util.Networking;
import static io.cockroachdb.pest.util.ProcessUtils.executeCommand;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private static final String OPERATOR_SCRIPT = "./pop";

    @Autowired
    private ApplicationSettings applicationSettings;

    @Override
    public boolean supports(ClusterType clusterType) {
        return false;
    }

    private void addNetworkingFlags(ClusterSettings clusterSettings,
                                    NodeSettings nodeSettings,
                                    List<String> args) {
        if (StringUtils.hasLength(nodeSettings.getListenAddr())) {
            args.add("--listen-addr=" + nodeSettings.getListenAddr());
        }

        if (applicationSettings.getToxiproxy().isEnabled()) {
            args.add("--advertise-addr=" +
                     Objects.requireNonNull(nodeSettings.getAdvertiseProxyAddr()));
        } else {
            if (StringUtils.hasLength(nodeSettings.getAdvertiseAddr())) {
                args.add("--advertise-addr=" + nodeSettings.getAdvertiseAddr());
            } else {
                if (StringUtils.hasLength(nodeSettings.getListenAddr())) {
                    args.add("--advertise-addr=" + nodeSettings.getListenAddr());
                } else {
                    args.add("--advertise-addr=" + Networking.getCanonicalHostName() + ":26257");
                }
            }
        }

        if (StringUtils.hasLength(nodeSettings.getSqlAddr())) {
            args.add("--sql-addr=" + nodeSettings.getSqlAddr());
        }

        if (StringUtils.hasLength(nodeSettings.getHttpAddr())) {
            args.add("--http-addr=" + nodeSettings.getHttpAddr());
        }

        if (clusterSettings.isSecure()) {
            args.add("--secure");
        }
    }

    @Override
    public String certs(ClusterSettings clusterSettings, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        // First create CA cert and key pairs
        executeCommand(applicationSettings.getBaseDirPath(), List.of(OPERATOR_SCRIPT, "cert"));

        // Then create node cert and key pairs
        clusterSettings.getNodes().forEach(nodeProperties -> {
            List<Path> expectedFiles = new ArrayList<>();
            expectedFiles.add(applicationSettings.getCertsDirPath()
                    .resolve(nodeProperties.getName()).resolve("node.crt"));
            expectedFiles.add(applicationSettings.getCertsDirPath()
                    .resolve(nodeProperties.getName()).resolve("node.key"));

            List<String> command = new ArrayList<>(List.of(OPERATOR_SCRIPT, "node-cert"));
            command.add("--name=" + nodeProperties.getName());
            command.addAll(nodeProperties.getCertHosts());

            executeCommand(applicationSettings.getBaseDirPath(), command);

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
    public String install(ClusterSettings clusterSettings, Integer nodeId) {
        List<String> args = List.of(OPERATOR_SCRIPT, "install",
                "--version=" + clusterSettings.getVersion()
        );
        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String init(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "init"));
        addNetworkingFlags(clusterSettings, nodeSettings, args);

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String wipe(ClusterSettings clusterSettings, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "wipe"));
        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String startNode(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        Map<Locality, List<String>> hosts = new TreeMap<>();

        clusterSettings.getNodes()
                .forEach(np -> {
                    Locality locality = Locality.fromTiers(np.getLocality());
                    hosts.computeIfAbsent(locality, x -> new ArrayList<>())
                            .add(Objects.requireNonNull(np.getAdvertiseAddr()));
                });

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start"));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + nodeSettings.getLocality());

        addNetworkingFlags(clusterSettings, nodeSettings, args);

        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(hosts)));

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String stopNode(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop"));
        addNetworkingFlags(clusterSettings, nodeSettings, args);

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String killNode(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "kill"));
        addNetworkingFlags(clusterSettings, nodeSettings, args);

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String sqlNode(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "sql"));
        addNetworkingFlags(clusterSettings, nodeSettings, args);

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String disruptNode(ClusterSettings clusterSettings, Integer nodeId) {
        return killNode(clusterSettings, nodeId);
    }

    @Override
    public String recoverNode(ClusterSettings clusterSettings, Integer nodeId) {
        return startNode(clusterSettings, nodeId);
    }

    @Override
    public String disruptLocality(ClusterSettings cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverLocality(ClusterSettings cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startProxyServer(ClusterSettings cluster) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-proxy"));
        args.add("--toxiproxy-host=" + applicationSettings.getToxiproxy().getHost());
        args.add("--toxiproxy-port=" + applicationSettings.getToxiproxy().getPort());
        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String startProxyClient(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-proxy-cli"));
        args.add("--toxiproxy-host=" + applicationSettings.getToxiproxy().getHost());
        args.add("--toxiproxy-port=" + applicationSettings.getToxiproxy().getPort());
        args.add("--listen_addr=" + nodeSettings.getAdvertiseAddr());
        args.add("--upstream_addr=" + nodeSettings.getListenAddr());
        args.add("--name=" + nodeSettings.getName());

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String stopProxyServer(ClusterSettings cluster) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-proxy"));
        args.add("--toxiproxy-host=" + applicationSettings.getToxiproxy().getHost());
        args.add("--toxiproxy-port=" + applicationSettings.getToxiproxy().getPort());
        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String startLoadBalancer(ClusterSettings clusterSettings, Integer nodeId) {
        NodeSettings nodeSettings = clusterSettings.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-lb"));
        args.add("--advertise-addr=" + nodeSettings.getAdvertiseAddr());

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }

    @Override
    public String stopLoadBalancer(ClusterSettings cluster) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-lb"));

        return executeCommand(applicationSettings.getBaseDirPath(), args).getFirst();
    }
}

package io.cockroachdb.pest.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.shell.ToxiproxyCommands;
import io.cockroachdb.pest.util.CommandException;
import io.cockroachdb.pest.util.Networking;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String OPERATOR_SCRIPT = "./pop";

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ToxiproxyCommands toxiproxyCommands;

    private String executeCommand(Path directory, List<String> commands) {
        try {
            Instant start = Instant.now();

            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(directory.toFile())
                    .inheritIO()
                    .start();

            logger.info("Process pid %s started: %s"
                    .formatted(process.pid(),
                            process.info().commandLine().orElse("")));

            while (process.isAlive()) {
                if (Thread.interrupted()) {
                    process.destroyForcibly();
                    break;
                }
                if (process.waitFor(5, TimeUnit.SECONDS)) {
                    logger.info("Waiting for process %s (%s)..."
                            .formatted(process.pid(),
                                    process.info().totalCpuDuration().orElse(Duration.ofSeconds(0))));
                    break;
                }
            }

            int code = process.exitValue();

            logger.info("Process pid %s finished with exit code %d (%s)"
                    .formatted(process.pid(),
                            code,
                            Duration.between(start, Instant.now())));

            return "Code " + code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Timeout waiting for process completion", e);
        } catch (IOException e) {
            throw new CommandException("Process I/O error", e);
        }
    }

    private void addServerNetworkingFlags(Cluster cluster,
                                          Cluster.Node node,
                                          List<String> args) {

        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            args.add("--advertise-addr=" +
                     Objects.requireNonNull(node.getAdvertiseProxyAddr(),
                             "advertise-proxy-addr required for node " + node.getId()));
        } else {
            if (StringUtils.hasLength(node.getAdvertiseAddr())) {
                args.add("--advertise-addr=" + node.getAdvertiseAddr());
            }
        }

        if (StringUtils.hasLength(node.getListenAddr())) {
            args.add("--listen-addr=" + node.getListenAddr());
        }

        if (StringUtils.hasLength(node.getSqlAddr())) {
            args.add("--sql-addr=" + node.getSqlAddr());
        }

        if (StringUtils.hasLength(node.getHttpAddr())) {
            args.add("--http-addr=" + node.getHttpAddr());
        }

        if (cluster.isSecure()) {
            args.add("--secure");
        }
    }

    private void addClientNetworkingFlags(Cluster cluster,
                                          Cluster.Node node,
                                          List<String> args) {
        args.add("--rpc-addr=" + node.getRpcAddr());
        args.add("--sql-addr=" + node.getSqlAddr());

        if (cluster.isSecure()) {
            args.add("--secure");
        }
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return false;
    }

    @Override
    public String certs(Cluster cluster, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        // First create CA cert and key pairs
        executeCommand(applicationProperties.getDirectories().getBaseDirPath(), List.of(OPERATOR_SCRIPT, "cert"));

        // Then create node cert and key pairs
        cluster.getNodes().forEach(node -> {
            List<Path> expectedFiles = new ArrayList<>();
            expectedFiles.add(applicationProperties.getDirectories().getCertsDirPath()
                    .resolve(node.getName()).resolve("node.crt"));
            expectedFiles.add(applicationProperties.getDirectories().getCertsDirPath()
                    .resolve(node.getName()).resolve("node.key"));

            List<String> certHosts = node.getCertHosts();
            if (certHosts.isEmpty()) {
                throw new RuntimeException("Missing cert hosts for node: " + node.getId());
            }

            List<String> command = new ArrayList<>(List.of(OPERATOR_SCRIPT, "node-cert"));
            command.add("--name=" + node.getName());
            command.addAll(certHosts);

            executeCommand(applicationProperties.getDirectories().getBaseDirPath(), command);

            expectedFiles.forEach(path -> {
                if (!Files.isReadable(path)) {
                    throw new UncheckedIOException(
                            new IOException("Expected node key file not found or readable: " + path));
                }
            });

            keyFiles.put(node.getId(), expectedFiles);
        });

        return "";
    }

    @Override
    public String install(Cluster cluster, Integer nodeId) {
        List<String> args = List.of(OPERATOR_SCRIPT, "install",
                "--version=" + cluster.getVersion()
        );
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String init(Cluster cluster, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "init"));
        addClientNetworkingFlags(cluster, cluster.getNodeById(nodeId), args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String wipe(Cluster cluster, Integer nodeId) {
        try {
            ApplicationProperties.Directories directories = applicationProperties.getDirectories();
            FileSystemUtils.deleteRecursively(directories.getBinDirPath());
            FileSystemUtils.deleteRecursively(directories.getCertsDirPath());
            FileSystemUtils.deleteRecursively(directories.getDataDirPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return "";
    }

    @Override
    public String startNode(Cluster cluster, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.createProxy("" + nodeId);
        }

        Map<Locality, List<String>> joinHosts = new TreeMap<>();

        cluster.getNodes()
                .forEach(np -> {
                    joinHosts.computeIfAbsent(Locality.fromTiers(np.getLocality()),
                            x -> new ArrayList<>()).add(np.getRpcAddr());
                });

        Cluster.Node node = cluster.getNodeById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start"));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + node.getLocality());
        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(joinHosts)));

        addServerNetworkingFlags(cluster, node, args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String stopNode(Cluster cluster, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.deleteProxy("" + nodeId);
        }

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop"));

        addServerNetworkingFlags(cluster, cluster.getNodeById(nodeId), args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String killNode(Cluster cluster, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.deleteProxy("" + nodeId);
        }

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "kill"));
        addServerNetworkingFlags(cluster, cluster.getNodeById(nodeId), args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String sqlNode(Cluster cluster, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "sql"));
        addClientNetworkingFlags(cluster, cluster.getNodeById(nodeId), args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String statusNode(Cluster cluster, Integer nodeId) {
        Cluster.Node node = cluster.getNodeById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "status"));
        args.add("--url=postgres://%s".formatted(node.getSqlAddr()));
        args.add("--format=records");

        if (cluster.isSecure()) {
            args.add("--secure");
        }

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String disruptNode(Cluster cluster, Integer nodeId) {
        return killNode(cluster, nodeId);
    }

    @Override
    public String recoverNode(Cluster cluster, Integer nodeId) {
        return startNode(cluster, nodeId);
    }

    @Override
    public String disruptLocality(Cluster cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverLocality(Cluster cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String startToxiproxyServer() {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-toxiproxy"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiproxy().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiproxy().getPort());
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String stopToxiproxyServer() {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-toxiproxy"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiproxy().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiproxy().getPort());
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String genHAProxyCfg(Cluster cluster, Integer nodeId) {
        try {
            Path templateFile = applicationProperties.getDirectories().getBaseDirPath()
                    .resolve("config")
                    .resolve(cluster.isSecure()
                            ? "haproxy-secure-template.cfg"
                            : "haproxy-insecure-template.cfg");

            String templateContents =
                    new PropertyPlaceholderHelper("${", "}")
                            .replacePlaceholders(Files.readString(templateFile),
                                    placeholderName -> switch (placeholderName.toLowerCase()) {
                                        case "bind-stats" ->
                                                "bind %s".formatted(cluster.getLoadBalancer().getStatsAddr());
                                        case "bind-rpc" ->
                                                "bind %s".formatted(cluster.getLoadBalancer().getRpcAddr());
                                        case "servers-rpc" -> {
                                            List<String> servers = new ArrayList<>();

                                            cluster.getNodes().forEach(np -> {
                                                servers.add("server cockroach%d %s check port %s\n"
                                                        .formatted(np.getId(),
                                                                np.getRpcAddr(),
                                                                Networking.getPort(np.getHttpAddr())
                                                        ));
                                            });

                                            yield String.join("    ", servers);
                                        }
                                        case "bind-http" ->
                                                "bind %s".formatted(cluster.getLoadBalancer().getHttpAddr());
                                        case "servers-http" -> {
                                            List<String> servers = new ArrayList<>();

                                            cluster.getNodes().forEach(np -> {
                                                servers.add("server cockroach%d %s check port %s\n"
                                                        .formatted(np.getId(),
                                                                np.getHttpAddr(),
                                                                Networking.getPort(np.getHttpAddr())
                                                        ));
                                            });

                                            yield String.join("   ", servers);
                                        }
                                        default -> placeholderName;
                                    });

            Path configFile = applicationProperties.getDirectories().getDataDirPath().resolve("haproxy.cfg");
            Files.writeString(configFile, templateContents,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            logger.info("Created " + configFile + " from " + templateFile);

            return "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String startHAProxy(Cluster cluster, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-haproxy"));
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String stopHAProxy(Cluster cluster, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-haproxy"));
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }
}

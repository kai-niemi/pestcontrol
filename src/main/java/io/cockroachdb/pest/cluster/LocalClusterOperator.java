package io.cockroachdb.pest.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;
import io.cockroachdb.pest.domain.Locality;
import io.cockroachdb.pest.shell.ToxiproxyCommands;
import io.cockroachdb.pest.util.CommandException;
import io.cockroachdb.pest.domain.NetworkAddress;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String OPERATOR_SCRIPT = "./pest-op";

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ToxiproxyCommands toxiproxyCommands;

    private String executeCommand(Path directory, List<String> commands) {
        if (applicationProperties.isDryRunLocalCommands()) {
            logger.info("Starting process (DRY RUN): %s".formatted(String.join("\n\t", commands)));
            return "";
        }

        try {
            logger.info("Starting process: %s".formatted(String.join("\n\t", commands)));

            Instant start = Instant.now();

            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(directory.toFile())
                    .inheritIO()
                    .start();


            try {
                logger.info("Process (pid %s) started - waiting for termination: %s"
                        .formatted(process.pid(),
                                process.info().commandLine().orElse("")));

                int code = process.waitFor();

                if (code != 0) {
                    logger.warn("Process (pid %s) terminated with exit code %d (%s)"
                            .formatted(process.pid(), code,
                                    Duration.between(start, Instant.now())));
                } else {
                    logger.info("Process (pid %s) terminated with exit code %d (%s)"
                            .formatted(process.pid(), code,
                                    Duration.between(start, Instant.now())));
                }

                return "" + code;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                logger.warn("Process (pid %s) wait timeout - forcibly closing..".formatted(process.pid()));

                process.destroyForcibly();
                process.onExit().join();

                logger.warn("Process (pid %s) wait timeout  - forcibly closed".formatted(process.pid()));

                throw new CommandException("Timeout waiting for process completion", e);
            }
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
        args.add("--rpc-addr=" + node.getJoinAddress());

        if (NetworkAddress.from(node.getSqlAddr()).getAddress().isEmpty()) {
            args.add("--sql-addr=" + NetworkAddress.getLoopbackHostname() + node.getSqlAddr());
        } else {
            args.add("--sql-addr=" + node.getSqlAddr());
        }

        if (cluster.isSecure()) {
            args.add("--secure");
        }
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure)
                .contains(clusterType);
    }

    @Override
    public String certs(Cluster cluster, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        if (!EnumSet.of(ClusterType.hosted_secure, ClusterType.local_secure).contains(cluster.getClusterType())) {
            throw new UnsupportedOperationException("Cluster '%s' is not of secure type: %s"
                    .formatted(cluster.getClusterId(), cluster.getClusterType()));
        }

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
            if (certHosts.isEmpty() && !applicationProperties.isDryRunLocalCommands()) {
                throw new InvalidConfigurationException("Missing cert hosts for node: " + node.getId());
            }

            List<String> command = new ArrayList<>(List.of(OPERATOR_SCRIPT, "node-cert"));
            command.add("--name=" + node.getName());
            command.addAll(certHosts);

            executeCommand(applicationProperties.getDirectories().getBaseDirPath(), command);

            if (!applicationProperties.isDryRunLocalCommands()) {
                expectedFiles.forEach(path -> {
                    if (!Files.isReadable(path)) {
                        throw new UncheckedIOException(
                                new IOException("Expected node key file not found or readable: " + path));
                    }
                });
            }

            keyFiles.put(node.getId(), expectedFiles);
        });

        return "";
    }

    @Override
    public String install(Cluster cluster, Integer nodeId) {
        List<String> args = List.of(OPERATOR_SCRIPT, "install",
                "--version=" + cluster.getNodeById(nodeId).getVersion()
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
    public String wipe(Cluster cluster, Integer nodeId, boolean all) {
        try {
            ApplicationProperties.Directories directories = applicationProperties.getDirectories();

            wipePath(directories.getCertsDirPath());
            wipePath(directories.getDataDirPath());
            if (all) {
                wipePath(directories.getBinDirPath());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return "";
    }

    private static void wipePath(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            FileSystemUtils.deleteRecursively(path);
            Files.deleteIfExists(path);
        }
    }

    @Override
    public String startNode(Cluster cluster, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.createProxy("" + nodeId);
        }

        Map<Locality, List<String>> joinHosts = new TreeMap<>();

        cluster.getNodes().forEach(node -> {
            String joinAddress = node.getJoinAddress();
            joinHosts.computeIfAbsent(Locality.fromTiers(node.getLocality()),
                            x -> new ArrayList<>())
                    .add(joinAddress);
        });

        Cluster.Node node = cluster.getNodeById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start"));
        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(joinHosts)));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + node.getLocality());

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
            Path templateFile = applicationProperties.getDirectories().getConfigDirPath()
                    .resolve(cluster.isSecure() ? "haproxy-secure.cfg" : "haproxy-insecure.cfg");

            String haproxyConfig =
                    "# DO NOT EDIT - file is overwritten by gen-haproxy command\n\n"
                    + Files.readString(templateFile);

            haproxyConfig = new PropertyPlaceholderHelper("${", "}")
                    .replacePlaceholders(haproxyConfig,
                            placeholderName -> switch (placeholderName.toLowerCase()) {
                                case "bind-stats" -> "bind %s".formatted(cluster.getLoadBalancer().getStatsAddr());
                                case "bind-rpc" -> "bind %s".formatted(cluster.getLoadBalancer().getRpcAddr());
                                case "servers-rpc" -> {
                                    List<String> servers = new ArrayList<>();
                                    cluster.getNodes().forEach(np -> {
                                        servers.add("server cockroach%d %s check port %s\n"
                                                .formatted(np.getId(),
                                                        np.getJoinAddress(),
                                                        NetworkAddress.from(np.getHttpAddr()).getPort().orElse(8080)
                                                ));
                                    });
                                    yield String.join("    ", servers);
                                }
                                case "bind-http" -> "bind %s".formatted(cluster.getLoadBalancer().getHttpAddr());
                                case "servers-http" -> {
                                    List<String> servers = new ArrayList<>();
                                    cluster.getNodes().forEach(np -> {
                                        servers.add("server cockroach%d %s check port %s\n"
                                                .formatted(np.getId(),
                                                        np.getHttpAddr(),
                                                        NetworkAddress.from(np.getHttpAddr()).getPort().orElse(8080)
                                                ));
                                    });
                                    yield String.join("    ", servers);
                                }
                                default -> placeholderName;
                            });

            Path configFilePath = applicationProperties.getDirectories().getConfigDirPath()
                    .resolve("haproxy.cfg");

            Files.writeString(configFilePath, haproxyConfig,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            logger.info("Created '" + configFilePath + "' from " + templateFile);

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

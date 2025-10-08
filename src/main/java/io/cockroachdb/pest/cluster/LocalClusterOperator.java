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
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.NodeProperties;
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
        Instant start = Instant.now();

        try {
            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(directory.toFile())
                    .inheritIO()
                    .start();

            logger.info("Process started: %s"
                    .formatted(process.info().commandLine().orElse("")));

            while (process.isAlive()) {
                if (Thread.interrupted()) {
                    process.destroyForcibly();
                    break;
                }
                if (process.waitFor(5, TimeUnit.SECONDS)) {
                    logger.info("Waiting... %s"
                            .formatted(process.info().totalCpuDuration().orElse(Duration.ofSeconds(0))));
                    break;
                }
            }

            int code = process.exitValue();

            logger.info("Process finished with exit code %d (%s)"
                    .formatted(code, Duration.between(start, Instant.now())));

            return "Code " + code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Timeout waiting for process completion", e);
        } catch (IOException e) {
            throw new CommandException("Process I/O error", e);
        }
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return false;
    }

    private void addServerNetworkingFlags(ClusterProperties clusterProperties,
                                          NodeProperties nodeProperties,
                                          List<String> args) {

        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            args.add("--advertise-addr=" +
                     Objects.requireNonNull(nodeProperties.getAdvertiseProxyAddr(),
                             "advertise-proxy-addr required for node " + nodeProperties.getId()));
        } else {
            if (StringUtils.hasLength(nodeProperties.getAdvertiseAddr())) {
                args.add("--advertise-addr=" + nodeProperties.getAdvertiseAddr());
            }
        }

        if (StringUtils.hasLength(nodeProperties.getListenAddr())) {
            args.add("--listen-addr=" + nodeProperties.getListenAddr());
        }

        if (StringUtils.hasLength(nodeProperties.getSqlAddr())) {
            args.add("--sql-addr=" + nodeProperties.getSqlAddr());
        }

//        if (StringUtils.hasLength(nodeProperties.getSqlAddr())) {
//            args.add("--advertise-sql-addr=" + nodeProperties.getSqlAddr());
//        }

        if (StringUtils.hasLength(nodeProperties.getHttpAddr())) {
            args.add("--http-addr=" + nodeProperties.getHttpAddr());
        }

        if (clusterProperties.isSecure()) {
            args.add("--secure");
        }
    }

    private void addClientNetworkingFlags(ClusterProperties clusterProperties,
                                          NodeProperties nodeProperties,
                                          List<String> args) {
        args.add("--rpc-addr=" + nodeProperties.getRpcAddr());
        args.add("--sql-addr=" + nodeProperties.getSqlAddr());

        if (clusterProperties.isSecure()) {
            args.add("--secure");
        }
    }

    @Override
    public String certs(ClusterProperties clusterProperties, List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        // First create CA cert and key pairs
        executeCommand(applicationProperties.getDirectories().getBaseDirPath(), List.of(OPERATOR_SCRIPT, "cert"));

        // Then create node cert and key pairs
        clusterProperties.getNodes().forEach(nodeProperties -> {
            List<Path> expectedFiles = new ArrayList<>();
            expectedFiles.add(applicationProperties.getDirectories().getCertsDirPath()
                    .resolve(nodeProperties.getName()).resolve("node.crt"));
            expectedFiles.add(applicationProperties.getDirectories().getCertsDirPath()
                    .resolve(nodeProperties.getName()).resolve("node.key"));

            List<String> certHosts = nodeProperties.getCertHosts();
            if (certHosts.isEmpty()) {
                throw new RuntimeException("Missing cert hosts for node: " + nodeProperties.getId());
            }

            List<String> command = new ArrayList<>(List.of(OPERATOR_SCRIPT, "node-cert"));
            command.add("--name=" + nodeProperties.getName());
            command.addAll(certHosts);

            executeCommand(applicationProperties.getDirectories().getBaseDirPath(), command);

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
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "init"));
        addClientNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String wipe(ClusterProperties clusterProperties, Integer nodeId) {
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
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.createProxy("" + nodeId);
        }

        Map<Locality, List<String>> joinHosts = new TreeMap<>();

        clusterProperties.getNodes()
                .forEach(np -> {
                    joinHosts.computeIfAbsent(Locality.fromTiers(np.getLocality()),
                            x -> new ArrayList<>()).add(np.getRpcAddr());
                });

        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start"));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + nodeProperties.getLocality());
        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(joinHosts)));

        addServerNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.deleteProxy("" + nodeId);
        }

        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop"));

        addServerNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        if (toxiproxyCommands.ifToxiproxy().isAvailable()) {
            toxiproxyCommands.deleteProxy("" + nodeId);
        }

        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "kill"));
        addServerNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String sqlNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "sql"));
        addClientNetworkingFlags(clusterProperties, nodeProperties, args);

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String statusNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "status"));
        args.add("--url=postgres://%s".formatted(nodeProperties.getSqlAddr()));
        args.add("--format=records");

        if (clusterProperties.isSecure()) {
            args.add("--secure");
        }

        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
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
    public String startToxiproxyServer() {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-toxiproxy"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiproxyProperties().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiproxyProperties().getPort());
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String stopToxiproxyServer() {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-toxiproxy"));
        args.add("--toxiproxy-host=" + applicationProperties.getToxiproxyProperties().getHost());
        args.add("--toxiproxy-port=" + applicationProperties.getToxiproxyProperties().getPort());
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String genHAProxyCfg(ClusterProperties clusterProperties, Integer nodeId) {
        try {
            Path templateFile = applicationProperties.getDirectories().getBaseDirPath()
                    .resolve("config")
                    .resolve(clusterProperties.isSecure()
                            ? "haproxy-secure-template.cfg"
                            : "haproxy-insecure-template.cfg");

            String templateContents =
                    new PropertyPlaceholderHelper("${", "}")
                            .replacePlaceholders(Files.readString(templateFile),
                                    placeholderName -> switch (placeholderName.toLowerCase()) {
                                        case "bind-rpc" -> {
                                            yield "    bind :".formatted(clusterProperties.getClusterId());
                                        }
                                        case "servers-rpc" -> {
                                            List<String> servers = new ArrayList<>();

                                            clusterProperties.getNodes().forEach(np -> {
                                                servers.add("    server cockroach%d %s check port %s\n"
                                                        .formatted(np.getId(),
                                                                np.getRpcAddr(),
                                                                Networking.getPort(np.getHttpAddr())
                                                        ));
                                            });

                                            yield String.join("", servers);
                                        }
                                        case "bind-http" -> {
                                            yield "    bind :".formatted(clusterProperties.getClusterId());
                                        }
                                        case "servers-http" -> {
                                            List<String> servers = new ArrayList<>();

                                            clusterProperties.getNodes().forEach(np -> {
                                                servers.add("    server cockroach%d %s check port %s\n"
                                                        .formatted(np.getId(),
                                                                np.getHttpAddr(),
                                                                Networking.getPort(np.getHttpAddr())
                                                        ));
                                            });

                                            yield String.join("", servers);
                                        }
                                        default -> placeholderName;
                                    });

            Path configFile = applicationProperties.getDirectories().getDataDirPath().resolve("haproxy.cfg");
            Files.writeString(configFile, templateContents, StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Created " + configFile + " from " + templateFile);

            return "";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String startHAProxy(ClusterProperties clusterProperties, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "start-haproxy"));
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }

    @Override
    public String stopHAProxy(ClusterProperties cluster, Integer nodeId) {
        List<String> args = new ArrayList<>(List.of(OPERATOR_SCRIPT, "stop-haproxy"));
        return executeCommand(applicationProperties.getDirectories().getBaseDirPath(), args);
    }
}

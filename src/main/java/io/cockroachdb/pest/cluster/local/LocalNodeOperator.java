package io.cockroachdb.pest.cluster.local;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.util.FileSystemUtils;

import io.cockroachdb.pest.cluster.InvalidConfigurationException;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;
import io.cockroachdb.pest.domain.Locality;
import static io.cockroachdb.pest.cluster.local.CommandBuilder.OPERATOR_SCRIPT;

public class LocalNodeOperator implements NodeOperator {
    private final Cluster cluster;

    private final ApplicationProperties applicationProperties;

    private final Path baseDir;

    public LocalNodeOperator(Cluster cluster, ApplicationProperties applicationProperties) {
        this.cluster = cluster;
        this.applicationProperties = applicationProperties;
        this.baseDir = applicationProperties.getDirectories().getBaseDirPath();
    }

    @Override
    public String certs(List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) {
        if (!EnumSet.of(ClusterType.hosted_secure, ClusterType.local_secure).contains(cluster.getClusterType())) {
            throw new UnsupportedOperationException("Cluster '%s' is not of secure type: %s"
                    .formatted(cluster.getClusterId(), cluster.getClusterType()));
        }

        // First create CA cert and key pairs
        CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("cert")
                .execute();

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

            CommandBuilder.builder()
                    .withBaseDir(baseDir)
                    .withCommand("node-cert")
                    .withFlags("--name=" + node.getName())
                    .withFlags(certHosts)
                    .execute();

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
    public String install(Integer nodeId) {
        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("install")
                .withFlags("--version=" + cluster.getNodeById(nodeId).getVersion())
                .execute();
    }

    @Override
    public String init(Integer nodeId) {
        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("init")
                .withClientNetworkingFlags(cluster, nodeId)
                .execute();
    }

    @Override
    public String wipe(Integer nodeId, boolean all) {
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
    public String startNode(Integer nodeId) {
        Map<Locality, List<String>> joinHosts = new TreeMap<>();

        cluster.getNodes().forEach(node -> {
            String joinAddress = node.getJoinAddress();
            joinHosts.computeIfAbsent(Locality.fromTiers(node.getLocality()),
                            x -> new ArrayList<>())
                    .add(joinAddress);
        });

        Cluster.Node node = cluster.getNodeById(nodeId);

        List<String> args = new ArrayList<>();
        args.add("--join=" + String.join(",", Locality.distributeJoinHosts(joinHosts)));
        args.add("--name=n" + nodeId);
        args.add("--locality=" + node.getLocality());

        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("start")
                .withFlags(args)
                .withServerNetworkingFlags(cluster, nodeId)
                .execute();
    }

    @Override
    public String stopNode(Integer nodeId) {
        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("stop")
                .withServerNetworkingFlags(cluster, nodeId)
                .execute();

    }

    @Override
    public String killNode(Integer nodeId) {
        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("kill")
                .withServerNetworkingFlags(cluster, nodeId)
                .execute();
    }

    @Override
    public String sqlNode(Integer nodeId) {
        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("sql")
                .withClientNetworkingFlags(cluster, nodeId)
                .execute();
    }

    @Override
    public String statusNode(Integer nodeId) {
        Cluster.Node node = cluster.getNodeById(nodeId);

        List<String> args = new ArrayList<>();
        args.add("--url=postgres://%s".formatted(node.getSqlAddr()));
        args.add("--format=records");

        if (cluster.isSecure()) {
            args.add("--secure");
        }

        return CommandBuilder.builder()
                .withBaseDir(baseDir)
                .withCommand("status")
                .withFlags(args)
                .execute();
    }

}

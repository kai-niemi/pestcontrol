package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.core.command.completion.CompletionProvider;
import org.springframework.shell.core.command.completion.CompositeCompletionProvider;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.shell.support.NodeRangeProvider;
import io.cockroachdb.pest.shell.support.VersionProvider;

@Component
public class NodeCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Bean
    public CompletionProvider nodeRangeProvider() {
        Cluster cluster = selectedCluster();
        return new NodeRangeProvider(cluster.getNodes().size());
    }

    @Bean
    public CompletionProvider versionProvider() {
        Cluster cluster = selectedCluster();
        return new CompositeCompletionProvider(
                new NodeRangeProvider(cluster.getNodes().size()),
                new VersionProvider("--version"));
    }

    @Command(description = "Create node certificates and key pairs",
            help = "Create and distribute node certificates and key pairs across all nodes. Usage: node certs --nodeId=<id>",
            name = {"generate", "certs"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedSecureCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void createCerts(@Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0)
                            String id) throws IOException {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster)
                .certs(nodeIdRange(id), new HashMap<>());
    }

    @Command(description = "Download and unpack the CockroachDB binary",
            help = "Download the CockroachDB binary assembly and unpack it on specified node(s)",
            name = {"install"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            completionProvider = "versionProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void installNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id,
            @Option(description = "CockroachDB version in 'vXX.y.z' format", defaultValue = "v25.4.3",
                    longName = "version") String version) throws IOException {

        if (Objects.nonNull(version) && !version.endsWith(".tgz")) {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            boolean isMac = os.getName().toLowerCase().contains("mac");
            boolean isLinux = os.getName().toLowerCase().contains("linux");
            boolean isArm = os.getArch().equalsIgnoreCase("aarch64")
                            || os.getArch().equalsIgnoreCase("arm64");
            if (isMac) {
                version = "%s.%s-%s".formatted(version, "darwin-11.0", isArm ? "arm64" : "amd64");
            } else if (isLinux) {
                version = "%s.%s-%s".formatted(version, "linux", isArm ? "arm64" : "amd64");
            }
        }

        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        for (Integer i : nodeIdRange(id)) {
            nodeOperator.install(i, version);
        }
    }

    @Command(description = "Run the init command",
            help = "Run the init command on specified node(s)",
            name = {"init"},
            completionProvider = "nodeRangeProvider",
            group = CommandGroups.NODE_COMMANDS,
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void initNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        nodeOperator.init(nodeIdRange(id).getFirst());
    }

    @Command(description = "Wipe local files",
            help = "Delete local database files, logs and certs on specified node(s)",
            name = {"wipe"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void wipeNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id,
            @Option(description = "Wipe install files", defaultValue = "false", longName = "all") boolean allFiles)
            throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        for (Integer x : nodeIdRange(id)) {
            nodeOperator.wipe(x, allFiles);
        }
    }

    @Command(description = "Start node",
            help = "Start node and (re)join cluster on specified node(s)",
            name = {"start"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void startNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        for (Integer i : nodeIdRange(id)) {
            nodeOperator.startNode(i);
        }
    }

    @Command(description = "Stop node",
            help = "Stop node on specified node(s)",
            name = {"stop"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void stopNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        for (Integer i : nodeIdRange(id)) {
            nodeOperator.stopNode(i);
        }
    }

    @Command(description = "Kill node",
            help = "Kill node forcefully on specified node(s)",
            name = {"kill"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void killNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        for (Integer i : nodeIdRange(id)) {
            nodeOperator.killNode(i);
        }
    }

    @Command(description = "Run sql shell",
            help = "Run SQL shell on this host and connect to a specified node",
            name = {"sql"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void sqlNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        nodeOperator.sqlNode(nodeIdRange(id).getFirst());
    }

    @Command(description = "Check node status",
            help = "Run status command on a specified node",
            name = {"status"},
            group = CommandGroups.NODE_COMMANDS,
            completionProvider = "nodeRangeProvider",
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void statusNode(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        nodeOperator.statusNode(nodeIdRange(id).getFirst());
    }
}

package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.domain.Cluster;

@Component
public class NodeCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Command(description = "Create node certificates and key pairs",
            help = "Create and distribute node certificates and key pairs across all nodes. Usage: node certs --nodeId=<id>",
            name = {"node", "certs"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedSecureCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void createCerts(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster)
                .certs(nodeIdRange(id), new HashMap<>());
    }

    @Command(description = "Download and unpack the CockroachDB binary",
            help = "Download the CockroachDB binary assembly and unpack it on specified node(s)",
            name = {"node", "install"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void installNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        for (Integer i : nodeIdRange(id)) {
            nodeOperator.install(i);
        }
    }

    @Command(description = "Run the init command",
            help = "Run the init command on specified node(s)",
            name = {"node", "init"},
            group = CommandGroups.NODE_COMMANDS,
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void initNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        nodeOperator.init(nodeIdRange(id).getFirst());
    }

    @Command(description = "Wipe local files",
            help = "Delete local database files, logs and certs on specified node(s)",
            name = {"node", "wipe"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void wipeNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id,
            @Option(description = "Also wipe install files", defaultValue = "false",
                    longName = "allfiles") boolean allFiles) throws IOException {
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
            name = {"node", "start"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void startNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
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
            name = {"node", "stop"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void stopNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
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
            name = {"node", "kill"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void killNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
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
            name = {"node", "sql"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void sqlNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        nodeOperator.sqlNode(nodeIdRange(id).getFirst());
    }

    @Command(description = "Check node status",
            help = "Run status command on a specified node",
            name = {"node", "status"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void statusNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        NodeOperator nodeOperator = clusterOperatorProvider
                .clusterOperator(cluster.getClusterId())
                .nodeOperator(cluster);
        nodeOperator.statusNode(nodeIdRange(id).getFirst());
    }
}

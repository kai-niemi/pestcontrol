package io.cockroachdb.pest.shell;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.model.Cluster;

@Component
public class NodeCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Command(description = "Create node certificates and key pairs",
            help = "Create and distribute node certificates and key pairs across all nodes. Usage: node certs --nodeId=<id>",
            name = {"node", "certs"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedSecureCluster")
    public void createCerts(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster).certs(cluster, nodeIdRange(id), new HashMap<>());
    }

    @Command(description = "Download and unpack the CockroachDB binary",
            help = "Download the CockroachDB binary assembly and unpack it on specified node(s)",
            name = {"node", "install"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void installNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        nodeIdRange(id).forEach(x -> clusterOperator.install(cluster, x));
    }

    @Command(description = "Run the init command",
            help = "Run the init command on specified node(s)",
            name = {"node", "init"}, group = CommandGroups.NODE_COMMANDS)
    public void initNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster)
                .init(cluster, nodeIdRange(id).getFirst());
    }

    @Command(description = "Wipe local files",
            help = "Delete local database files, logs and certs on specified node(s)",
            name = {"node", "wipe"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void wipeNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id,
            @Option(description = "Also wipe install files", defaultValue = "false",
                    longName = "allfiles") boolean allFiles) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        nodeIdRange(id).forEach(x -> clusterOperator.wipe(cluster, x, allFiles));
    }

    @Command(description = "Start node",
            help = "Start node and (re)join cluster on specified node(s)",
            name = {"node", "start"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void startNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        nodeIdRange(id).forEach(x -> clusterOperator.startNode(cluster, x));
    }

    @Command(description = "Stop node",
            help = "Stop node on specified node(s)",
            name = {"node", "stop"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void stopNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        nodeIdRange(id).forEach(x -> clusterOperator.stopNode(cluster, x));
    }

    @Command(description = "Kill node",
            help = "Kill node forcefully on specified node(s)",
            name = {"node", "kill"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void killNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        nodeIdRange(id).forEach(x -> clusterOperator.killNode(cluster, x));
    }

    @Command(description = "Run sql shell",
            help = "Run SQL shell on this host and connect to a specified node",
            name = {"node", "sql"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void sqlNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster).sqlNode(cluster, nodeIdRange(id).getFirst());
    }

    @Command(description = "Check node status",
            help = "Run status command on a specified node",
            name = {"node", "status"},
            group = CommandGroups.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void statusNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster).statusNode(cluster, nodeIdRange(id).getFirst());
    }
}

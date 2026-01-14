package io.cockroachdb.pest.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.util.PatternUtils;

@Component
public class ChaosCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Command(description = "Disrupt specified node(s)",
            name = {"chaos", "disrupt", "node"}, group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void disruptNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        PatternUtils.parseIntRange(id).forEach(x -> clusterOperator.disruptNode(cluster, x));
    }

    @Command(description = "Recover specified node(s)",
            name = {"chaos", "recover", "node"}, group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void recoverNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster);
        PatternUtils.parseIntRange(id).forEach(x -> clusterOperator.recoverNode(cluster, x));
    }

    @Command(description = "Disrupt nodes in a specified locality",
            name = {"chaos", "disrupt", "locality"}, group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void disruptLocality(
            @Option(description = "The locality tier(s) to disrupt", longName = "locality") String locality) {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster).disruptLocality(cluster, locality);
    }

    @Command(description = "Recover nodes in a specified locality",
            name = {"chaos", "recover", "locality"}, group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void recoverLocality(
            @Option(description = "The locality tier(s) to disrupt", longName = "locality") String locality) {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster).recoverLocality(cluster, locality);
    }

}

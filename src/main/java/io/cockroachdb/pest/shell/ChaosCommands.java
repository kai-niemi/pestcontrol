package io.cockroachdb.pest.shell;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.util.PatternUtils;

@Component
public class ChaosCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Command(description = "Disrupt specified node(s)",
            name = {"chaos", "disrupt", "node"},
            group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void disruptNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : PatternUtils.parseIntRange(id)) {
            clusterOperator.disruptionOperator(cluster).disruptNode(x);
        }
    }

    @Command(description = "Recover specified node(s)",
            name = {"chaos", "recover", "node"},
            group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void recoverNode(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : PatternUtils.parseIntRange(id)) {
            clusterOperator.disruptionOperator(cluster).recoverNode(x);
        }
    }

    @Command(description = "Disrupt nodes in a specified locality",
            name = {"chaos", "disrupt", "locality"},
            group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void disruptLocality(
            @Option(description = "The locality tier(s) to disrupt", longName = "locality") String locality)
            throws IOException {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster.getClusterId())
                .disruptionOperator(cluster)
                .disruptLocality(locality);
    }

    @Command(description = "Recover nodes in a specified locality",
            name = {"chaos", "recover", "locality"},
            group = CommandGroups.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void recoverLocality(
            @Option(description = "The locality tier(s) to disrupt", longName = "locality") String locality)
            throws IOException {
        Cluster cluster = selectedCluster();
        clusterOperatorProvider.clusterOperator(cluster.getClusterId())
                .disruptionOperator(cluster)
                .recoverLocality(locality);
    }

}

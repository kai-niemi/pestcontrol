package io.cockroachdb.pest.shell;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.util.PatternUtils;

@Component
public class ChaosCommands extends AbstractCommand {
    @Command(description = "Disrupt specified node(s)", name = {"disrupt"}, group = Constants.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void disruptNode(
            @Option(description = "Node IDs as comma separated list of 1-based ints and/or range", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.disruptNode(cluster, id));
    }

    @Command(description = "Recover specified node(s)", name = {"recover"}, group = Constants.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void recoverNode(
            @Option(description = "Node IDs as comma separated list of 1-based ints and/or range", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.recoverNode(cluster, id));
    }

    @Command(description = "Disrupt nodes in a specified locality", name = {
            "disrupt-locality"}, group = Constants.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void disruptLocality(
            @Option(description = "The locality tier(s) to disrupt", longName = "locality") String locality) {
        Cluster cluster = getSelectedCluster();
        getClusterOperator(cluster).disruptLocality(cluster, locality);
    }

    @Command(description = "Recover nodes in a specified locality", name = {
            "recover-locality"}, group = Constants.CHAOS_COMMANDS,
            availabilityProvider = "ifCockroachCloudCluster")
    public void recoverLocality(
            @Option(description = "The locality tier(s) to disrupt", longName = "locality") String locality) {
        Cluster cluster = getSelectedCluster();
        getClusterOperator(cluster).recoverLocality(cluster, locality);
    }

}

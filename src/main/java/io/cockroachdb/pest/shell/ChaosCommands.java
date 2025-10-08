package io.cockroachdb.pest.shell;

import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.CHAOS_COMMANDS)
public class ChaosCommands extends AbstractCommand {
    public Availability ifCockroachCloudCluster() {
        return ifClusterSelected().isAvailable()
               && ClusterTypes.isCloud(CLUSTER_PROPERTIES.getClusterType())
                ? Availability.available()
                : Availability.unavailable("cluster type is not cockroach cloud!");
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Disrupt specified node(s)", key = {"disrupt"})
    public void disruptNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        Cluster cluster = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(cluster.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.disruptNode(cluster, id));
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Recover specified node(s)", key = {"recover"})
    public void recoverNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        Cluster cluster = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(cluster.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.recoverNode(cluster, id));
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Disrupt nodes in a specified locality", key = {"disrupt-locality"})
    public void disruptLocality(
            @ShellOption(help = "The locality tier(s) to disrupt") String locality) {
        Cluster cluster = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(cluster.getClusterId());
        clusterOperator.disruptLocality(cluster, locality);
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Recover nodes in a specified locality", key = {"recover-locality"})
    public void recoverLocality(
            @ShellOption(help = "The locality tier(s) to disrupt") String locality) {
        Cluster cluster = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(cluster.getClusterId());
        clusterOperator.recoverLocality(cluster, locality);
    }

}

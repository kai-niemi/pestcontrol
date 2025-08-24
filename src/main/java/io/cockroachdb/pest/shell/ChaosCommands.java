package io.cockroachdb.pest.shell;

import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.CHAOS_COMMANDS)
public class ChaosCommands extends AbstractCommand {
    public Availability ifCockroachCloudCluster() {
        return ifClusterSelected().isAvailable()
               && ClusterTypes.isCloud(CLUSTER_ID_SELECTION.get().getClusterType())
                ? Availability.available()
                : Availability.unavailable("cluster type is not cockroach cloud!");
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Disrupt specified node(s)", key = {"disrupt"})
    public void disruptNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.disruptNode(clusterSettings, id));
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Recover specified node(s)", key = {"recover"})
    public void recoverNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.recoverNode(clusterSettings, id));
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Disrupt nodes in a specified locality", key = {"disrupt-locality"})
    public void disruptLocality(
            @ShellOption(help = "The locality tier(s) to disrupt") String locality) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        clusterOperator.disruptLocality(clusterSettings, locality);
    }

    @ShellMethodAvailability("ifCockroachCloudCluster")
    @ShellMethod(value = "Recover nodes in a specified locality", key = {"recover-locality"})
    public void recoverLocality(
            @ShellOption(help = "The locality tier(s) to disrupt") String locality) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        clusterOperator.recoverLocality(clusterSettings, locality);
    }

}

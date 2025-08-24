package io.cockroachdb.pest.shell;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.NODE_COMMANDS)
public class NodeCommands extends AbstractCommand {
    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install"})
    public void installNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.install(clusterSettings, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'init' command on specified node(s)", key = {"init"})
    public void initNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.init(clusterSettings, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'wipe' command on specified node(s)", key = {"wipe"})
    public void wipeNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.wipe(clusterSettings, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start"})
    public void startNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.startNode(clusterSettings, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop"})
    public void stopNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.stopNode(clusterSettings, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill"})
    public void killNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.killNode(clusterSettings, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'sql' command on this host and connect to a specified node", key = {"sql"})
    public void sqlNode(
            @ShellOption(help = "Node ID (1-based)") String node) {
        ClusterSettings clusterSettings = getClusterSettings();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterSettings.getClusterId());
        clusterOperator.sqlNode(clusterSettings, Integer.parseInt(node));
    }
}

package io.cockroachdb.pest.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;

@ShellComponent
@ShellCommandGroup(Constants.CLUSTER_COMMANDS)
public class ClusterCommands {
    @Autowired
    private ClusterManager clusterManager;

    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start"})
    public void startNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = "hosted-insecure") String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = "1") int nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.startNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.startNode(clusterProperties, nodeId);
        }
    }

    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop"})
    public void stopNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = "hosted-insecure") String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = "1") int nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.stopNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.stopNode(clusterProperties, nodeId);
        }
    }

    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill"})
    public void killNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = "hosted-insecure") String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = "1") int nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.killNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.killNode(clusterProperties, nodeId);
        }
    }

    @ShellMethod(value = "Run 'init' command on specified node(s)", key = {"init"})
    public void initNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = "hosted-insecure") String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = "1") int nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.init(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.init(clusterProperties, nodeId);
        }
    }

    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install"})
    public void installNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = "hosted-insecure") String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = "1") int nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.install(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.install(clusterProperties, nodeId);
        }
    }
}

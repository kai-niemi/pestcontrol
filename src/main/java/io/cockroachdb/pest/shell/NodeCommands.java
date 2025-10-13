package io.cockroachdb.pest.shell;

import java.util.HashMap;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.Cluster;

@ShellComponent
@ShellCommandGroup(Constants.NODE_COMMANDS)
public class NodeCommands extends AbstractCommand {
    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs", "c"})
    public void createCerts(@ShellOption(help = "Node IDs range or 'all'", defaultValue = "1") String nodes) {
        Cluster cluster = getSelectedCluster();
        getSelectedClusterOperator().certs(cluster, nodeIdRange(nodes), new HashMap<>());
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install", "l"})
    public void installNode(@ShellOption(help = "Node IDs range or 'all'", defaultValue = "1") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.install(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'init' command on specified node", key = {"init", "i"})
    public void initNode(@ShellOption(help = "Node ID (1-based)", defaultValue = "1") String node) {
        Cluster cluster = getSelectedCluster();
        getSelectedClusterOperator().init(cluster, nodeId(node));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'wipe' command on specified node(s)", key = {"wipe", "w"})
    public void wipeNode(@ShellOption(help = "Node IDs range or 'all'", defaultValue = "1") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.wipe(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start", "s"})
    public void startNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.startNode(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop", "p"})
    public void stopNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.stopNode(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill", "k"})
    public void killNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.killNode(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'sql' command on this host and connect to a specified node", key = {"sql"})
    public void sqlNode(@ShellOption(help = "Node ID (1-based)") String node) {
        Cluster cluster = getSelectedCluster();
        getSelectedClusterOperator().sqlNode(cluster, nodeId(node));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Run 'status' command on this host and connect to a specified node", key = {"status"})
    public void statusNode(@ShellOption(help = "Node ID (1-based)") String node) {
        Cluster cluster = getSelectedCluster();
        getSelectedClusterOperator().statusNode(cluster, nodeId(node));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Generate haproxy.cfg on specified hosts", key = {"gen-haproxy", "gha"})
    public void genHAProxy(@ShellOption(help = "Node IDs range or 'all'", defaultValue = "1") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.genHAProxyCfg(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Start haproxy load balancer on specified hosts", key = {"start-haproxy", "sha"})
    public void startHaProxy(@ShellOption(help = "Node IDs range or 'all'", defaultValue = "1") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.startHAProxy(cluster, id));
    }

    @ShellMethodAvailability("ifHostedCluster")
    @ShellMethod(value = "Stop haproxy load balancer on specified hosts", key = {"stop-haproxy", "pha"})
    public void stopHAProxy(@ShellOption(help = "Node IDs range or 'all'", defaultValue = "1") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getSelectedClusterOperator();
        nodeIdRange(nodes).forEach(id -> clusterOperator.stopHAProxy(cluster, id));
    }
}

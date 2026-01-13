package io.cockroachdb.pest.shell;

import java.util.HashMap;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.Cluster;

@Component
public class NodeCommands extends AbstractCommand {
    @Command(description = "Create and distribute node certificates and key pairs", name = {
            "certs", "c"}, group = Constants.NODE_COMMANDS, availabilityProvider = "ifSecureCluster")
    public void createCerts(
            @Option(description = "Node IDs range or 'all'", defaultValue = "1", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        getClusterOperator(cluster).certs(cluster, nodeIdRange(nodes), new HashMap<>());
    }

    @Command(description = "Run 'install' command on specified node(s)", name = {
            "install", "l"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void installNode(
            @Option(description = "Node IDs range or 'all'", defaultValue = "1", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.install(cluster, id));
    }

    @Command(description = "Run 'init' command on specified node", name = {
            "init", "i"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void initNode(
            @Option(description = "Node ID (1-based)", defaultValue = "1", shortName = 'e', longName = "node")
            String node) {
        Cluster cluster = getSelectedCluster();
        getClusterOperator(cluster).init(cluster, nodeId(node));
    }

    @Command(description = "Run 'wipe' command on specified node(s)", name = {
            "wipe", "w"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void wipeNode(
            @Option(description = "Node IDs range or 'all'", defaultValue = "1", shortName = 'n', longName = "nodes")
            String nodes,
            @Option(description = "Wipe all directories including install files", defaultValue = "false", longName = "all")
            Boolean all) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.wipe(cluster, id, all));
    }

    @Command(description = "Run 'start' command on specified node(s)", name = {
            "start", "s"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void startNode(
            @Option(description = "Node IDs range or 'all'", shortName = 'n', longName = "nodes") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.startNode(cluster, id));
    }

    @Command(description = "Run 'stop' command on specified node(s)", name = {
            "stop", "p"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void stopNode(
            @Option(description = "Node IDs range or 'all'", shortName = 'n', longName = "nodes") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.stopNode(cluster, id));
    }

    @Command(description = "Run 'kill' command on specified node(s)", name = {
            "kill", "k"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void killNode(
            @Option(description = "Node IDs range or 'all'", shortName = 'n', longName = "nodes") String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.killNode(cluster, id));
    }

    @Command(description = "Run 'sql' command on this host and connect to a specified node", name = {
            "sql"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void sqlNode(@Option(description = "Node ID (1-based)", shortName = 'e', longName = "node") String node) {
        Cluster cluster = getSelectedCluster();
        getClusterOperator(cluster).sqlNode(cluster, nodeId(node));
    }

    @Command(description = "Run 'status' command on this host and connect to a specified node", name = {
            "status"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void statusNode(@Option(description = "Node ID (1-based)", shortName = 'e', longName = "node") String node) {
        Cluster cluster = getSelectedCluster();
        getClusterOperator(cluster).statusNode(cluster, nodeId(node));
    }

    @Command(description = "Generate haproxy.cfg on specified hosts", name = {
            "gen-haproxy", "gha"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void genHAProxy(
            @Option(description = "Node IDs range or 'all'", defaultValue = "1", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.genHAProxyCfg(cluster, id));
    }

    @Command(description = "Start haproxy load balancer on specified hosts", name = {
            "start-haproxy", "sha"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void startHaProxy(
            @Option(description = "Node IDs range or 'all'", defaultValue = "1", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.startHAProxy(cluster, id));
    }

    @Command(description = "Stop haproxy load balancer on specified hosts", name = {
            "stop-haproxy", "pha"}, group = Constants.NODE_COMMANDS,
            availabilityProvider = "ifHostedCluster")
    public void stopHAProxy(
            @Option(description = "Node IDs range or 'all'", defaultValue = "1", shortName = 'n', longName = "nodes")
            String nodes) {
        Cluster cluster = getSelectedCluster();
        ClusterOperator clusterOperator = getClusterOperator(cluster);
        nodeIdRange(nodes).forEach(id -> clusterOperator.stopHAProxy(cluster, id));
    }
}

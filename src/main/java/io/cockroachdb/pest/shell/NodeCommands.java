package io.cockroachdb.pest.shell;

import java.util.HashMap;
import java.util.List;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.NODE_COMMANDS)
public class NodeCommands extends AbstractCommand {
    private List<Integer> range(String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        if (nodes.startsWith("all")) {
            return List.of(1, clusterProperties.getNodes().size());
        }
        return PatternUtils.parseIntRange(nodes);
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs"})
    public void createCerts(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.certs(clusterProperties, range(nodes), new HashMap<>());
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install"})
    public void installNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        range(nodes).forEach(id -> clusterOperator.install(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'init' command on specified node(s)", key = {"init"})
    public void initNode(
            @ShellOption(help = "Node ID (1-based int)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.init(clusterProperties, Integer.parseInt(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'wipe' command on specified node(s)", key = {"wipe"})
    public void wipeNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        range(nodes).forEach(id -> clusterOperator.wipe(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start"})
    public void startNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        range(nodes).forEach(id -> clusterOperator.startNode(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop"})
    public void stopNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        range(nodes).forEach(id -> clusterOperator.stopNode(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill"})
    public void killNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        range(nodes).forEach(id -> clusterOperator.killNode(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'sql' command on this host and connect to a specified node", key = {"sql"})
    public void sqlNode(
            @ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.sqlNode(clusterProperties, Integer.parseInt(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Generate haproxy.cfg on specified host", key = {"gen-haproxy"})
    public void genHAProxy(@ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.genHAProxyCfg(clusterProperties, Integer.parseInt(node));
    }
    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start load balancer on specified host", key = {"start-haproxy"})
    public void startHaProxy(@ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.startHAProxy(clusterProperties, Integer.parseInt(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Stop load balancer on specified host", key = {"stop-haproxy"})
    public void stopHAProxy() {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.stopHAProxy(clusterProperties);
    }
}

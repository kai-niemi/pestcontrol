package io.cockroachdb.pest.shell;

import java.util.HashMap;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;

@ShellComponent
@ShellCommandGroup(Constants.NODE_COMMANDS)
public class NodeCommands extends AbstractCommand {
    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs", "c"})
    public void createCerts(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.certs(clusterProperties, nodeIdRange(nodes), new HashMap<>());
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install", "l"})
    public void installNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.install(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'init' command on specified node", key = {"init", "i"})
    public void initNode(@ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.init(clusterProperties, nodeId(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'wipe' command on specified node(s)", key = {"wipe", "w"})
    public void wipeNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.wipe(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start", "s"})
    public void startNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.startNode(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop", "p"})
    public void stopNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.stopNode(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill", "k"})
    public void killNode(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.killNode(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'sql' command on this host and connect to a specified node", key = {"sql"})
    public void sqlNode(@ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.sqlNode(clusterProperties, nodeId(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'status' command on this host and connect to a specified node", key = {"status"})
    public void statusNode(@ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.statusNode(clusterProperties, nodeId(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Generate haproxy.cfg on specified hosts", key = {"gen-haproxy", "gha"})
    public void genHAProxy(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.genHAProxyCfg(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start haproxy load balancer on specified hosts", key = {"start-haproxy", "sha"})
    public void startHaProxy(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.startHAProxy(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Stop haproxy load balancer on specified hosts", key = {"stop-haproxy", "pha"})
    public void stopHAProxy(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        nodeIdRange(nodes).forEach(id -> clusterOperator.stopHAProxy(clusterProperties, id));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start toxiproxy server on local host", key = {"start-toxiproxy", "sto"})
    public void startToxiproxyServer() {
        getClusterOperator().startToxiproxyServer();
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Stop toxiproxy server on local host", key = {"stop-toxiproxy", "pto"})
    public void stopToxiproxyServer() {
        getClusterOperator().stopToxiproxyServer();
    }
}

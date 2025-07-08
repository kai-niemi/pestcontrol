package io.cockroachdb.pest.shell;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import eu.rekawek.toxiproxy.ToxiproxyClient;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.PROXY_COMMANDS)
public class ProxyCommands extends AbstractCommand {
    @Autowired
    private ToxiproxyClient toxiproxyClient;

    public Availability ifToxiProxyRunning() {
        if (ifClusterSelected().isAvailable()) {
            try {
                toxiproxyClient.version();
                return Availability.available();
            } catch (IOException e) {
                return Availability.unavailable("toxiproxy is not installed or not running (" + e.getMessage() + ")!");
            }
        } else {
            return ifClusterSelected();
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start toxiproxy server on this host", key = {"start-proxy"})
    public void startProxy() {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.startProxyServer(clusterProperties);
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Stop toxiproxy server on this host", key = {"stop-proxy"})
    public void stopProxy() {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.stopProxyServer(clusterProperties);
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Start toxiproxy client on specified node(s)", key = {"start-proxy-cli"})
    public void startProxyCli(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.startProxyClient(clusterProperties, id));
    }
}

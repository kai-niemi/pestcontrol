package io.cockroachdb.pest.shell;

import java.util.EnumSet;
import java.util.HashMap;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.shell.support.ClusterProvider;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.SETUP_COMMANDS)
public class SetupCommands extends AbstractCommand {
    @PostConstruct
    public void init() {
        if (StringUtils.hasLength(applicationProperties.getDefaultClusterId())) {
            selectClusterID(applicationProperties.getDefaultClusterId());
        }
    }

    @ShellMethod(value = "Select default cluster ID to use in commands", key = {"select-cluster", "sc"})
    public void selectClusterID(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId) {
        CLUSTER_ID_SELECTION.set(clusterManager.getClusterProperties(clusterId,
                EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs"})
    public void createCerts(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.certs(clusterProperties, PatternUtils.parseIntRange(nodes), new HashMap<>());
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start load balancer on this host", key = {"start-lb"})
    public void startLB(@ShellOption(help = "Node ID (1-based)") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.startLoadBalancer(clusterProperties, Integer.parseInt(node));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Stop load balancer on this host", key = {"stop-lb"})
    public void stopLB() {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.stopLoadBalancer(clusterProperties);
    }
}

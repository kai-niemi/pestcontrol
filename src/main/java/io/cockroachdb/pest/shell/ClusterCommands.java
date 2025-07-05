package io.cockroachdb.pest.shell;

import java.util.EnumSet;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.shell.support.ClusterProvider;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.CLUSTER_COMMANDS)
public class ClusterCommands {
    private static final ThreadLocal<ClusterProperties> CLUSTER_ID_SELECTION = ThreadLocal.withInitial(() -> null);

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    public Availability ifClusterSelected() {
        return Objects.isNull(CLUSTER_ID_SELECTION.get())
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    public ClusterProperties getClusterProperties(String clusterId) {
        if (Objects.isNull(clusterId) && Objects.isNull(CLUSTER_ID_SELECTION.get())) {
            throw new IllegalStateException("Cluster ID not specified");
        }
        return Objects.isNull(clusterId)
                ? CLUSTER_ID_SELECTION.get()
                : clusterManager.getClusterProperties(clusterId,
                EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure));
    }

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

    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install"})
    public void installNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId
            ) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.install(clusterProperties, id));
    }

    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs"})
    public void createCerts(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.certs(clusterProperties, PatternUtils.parseIntRange(nodes));
    }

    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start"})
    public void startNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.startNode(clusterProperties, id));
    }

    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop"})
    public void stopNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId
            ) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.stopNode(clusterProperties, id));
    }

    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill"})
    public void killNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.killNode(clusterProperties, id));
    }

    @ShellMethod(value = "Run 'init' command on specified node(s)", key = {"init"})
    public void initNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.init(clusterProperties, id));
    }

    @ShellMethod(value = "Run 'sql' command on local node", key = {"sql"})
    public void sqlNode(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes,
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.sqlNode(clusterProperties, id));
    }
}

package io.cockroachdb.pest.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.Availability;
import org.springframework.shell.jline.PromptProvider;
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
import io.cockroachdb.pest.shell.support.ClusterProvider;

@ShellComponent
@ShellCommandGroup(Constants.CLUSTER_COMMANDS)
public class ClusterCommands implements PromptProvider {
    public static final ThreadLocal<ClusterProperties> CLUSTER_ID_SELECTION = ThreadLocal.withInitial(() -> null);

    public static final ThreadLocal<Integer> NODE_ID_SELECTION = ThreadLocal.withInitial(() -> null);

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
                : clusterManager.getClusterProperties(clusterId);
    }

    public Integer getNodeId(Integer nodeId) {
        if (Objects.isNull(nodeId) && Objects.isNull(NODE_ID_SELECTION.get())) {
            throw new IllegalStateException("Node ID not specified (1-based)");
        }
        return Objects.isNull(nodeId)
                ? NODE_ID_SELECTION.get()
                : nodeId;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.hasLength(applicationProperties.getDefaultClusterId())) {
            selectClusterID(applicationProperties.getDefaultClusterId());
        }
    }

    @Override
    public AttributedString getPrompt() {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("pest", AttributedStyle.DEFAULT
                .foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT));

        ClusterProperties clusterProperties = CLUSTER_ID_SELECTION.get();
        if (Objects.isNull(clusterProperties)) {
            sb.append(" $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        } else {
            sb.append(" cluster:(", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
            sb.append(clusterProperties.getClusterId(), AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.RED | AttributedStyle.BRIGHT)
                    .faintOff());
            sb.append(") $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        }

        return sb.toAttributedString();
    }

    @ShellMethod(value = "Select default cluster ID to use in commands", key = {"default-cluster-id", "dci"})
    public void selectClusterID(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId) {
        CLUSTER_ID_SELECTION.set(clusterManager.getClusterProperties(clusterId));
    }

    @ShellMethod(value = "Select default node ID to use in commands", key = {"default-node-id", "dni"})
    public void selectNodeID(
            @ShellOption(help = "Node ID (1-based)") Integer nodeId) {
        NODE_ID_SELECTION.set(nodeId);
    }

    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs"})
    public void createCerts(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        List<Integer> nodeIds = new ArrayList<>();
        if (all) {
            clusterProperties.getNodes()
                    .forEach(nodeProperties -> nodeIds.add(nodeProperties.getId()));
        } else {
            nodeIds.add(nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
        clusterOperator.certs(clusterProperties, nodeIds);
    }


    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start"})
    public void startNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.startNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.startNode(clusterProperties, getNodeId(nodeId));
        }
    }


    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop"})
    public void stopNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.stopNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.stopNode(clusterProperties, getNodeId(nodeId));
        }
    }


    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill"})
    public void killNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.killNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.killNode(clusterProperties, getNodeId(nodeId));
        }
    }


    @ShellMethod(value = "Run 'init' command on specified node(s)", key = {"init"})
    public void initNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.init(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.init(clusterProperties, getNodeId(nodeId));
        }
    }


    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install"})
    public void installNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.install(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.install(clusterProperties, getNodeId(nodeId));
        }
    }


    @ShellMethod(value = "Run 'sql' command on local node", key = {"sql"})
    public void sqlNode(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL) String clusterId,
            @ShellOption(help = "Node ID (1-based)", defaultValue = ShellOption.NULL) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.install(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.sqlNode(clusterProperties, getNodeId(nodeId));
        }
    }
}

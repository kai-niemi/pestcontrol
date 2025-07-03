package io.cockroachdb.pest.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.shell.support.ClusterProvider;

@ShellComponent
@ShellCommandGroup(Constants.CLUSTER_COMMANDS)
public class ClusterCommands implements PromptProvider {
    public static final ThreadLocal<ClusterProperties> CLUSTER_SELECTION = ThreadLocal.withInitial(() -> null);

    public static final ThreadLocal<Integer> NODE_ID_SELECTION = ThreadLocal.withInitial(() -> null);

    public static final String DEFAULT_CLUSTER_ID = ShellOption.NULL;

    public static final String DEFAULT_NODE_ID = ShellOption.NULL;

    @Autowired
    private ClusterManager clusterManager;

    public Availability ifClusterSelected() {
        return Objects.isNull(CLUSTER_SELECTION.get())
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    @Override
    public AttributedString getPrompt() {
        AttributedStringBuilder sb = new AttributedStringBuilder();

        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();
        if (Objects.isNull(clusterProperties)) {
            sb.append("pest", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.CYAN | AttributedStyle.BRIGHT));
            sb.append(" $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        } else {
            sb.append("pest", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.CYAN | AttributedStyle.BRIGHT));
            sb.append(" cluster:(", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
            sb.append(clusterProperties.getClusterId(),
                    AttributedStyle.DEFAULT
                            .foreground(AttributedStyle.RED | AttributedStyle.BRIGHT)
                            .faintOff());
            sb.append(") $ ", AttributedStyle.DEFAULT
                    .foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT));
        }
        return sb.toAttributedString();
    }

    @ShellMethod(value = "Select cluster ID to use in commands", key = {"select"})
    public void selectClusterID(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)", valueProvider = ClusterProvider.class)
            String clusterId) {
        CLUSTER_SELECTION.set(clusterManager.getClusterProperties(clusterId));
    }

    @ShellMethod(value = "Select default node ID to use in commands", key = {"select-node"})
    public void selectNodeID(
            @ShellOption(help = "Node ID (1-based)") Integer nodeId) {
        NODE_ID_SELECTION.set(nodeId);
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Create and distribute node certificates and key pairs", key = {"certs"})
    public void createCerts(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

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

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'start' command on specified node(s)", key = {"start"})
    public void startNode(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.startNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.startNode(clusterProperties, nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'stop' command on specified node(s)", key = {"stop"})
    public void stopNode(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.stopNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.stopNode(clusterProperties, nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'kill' command on specified node(s)", key = {"kill"})
    public void killNode(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.killNode(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.killNode(clusterProperties, nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'init' command on specified node(s)", key = {"init"})
    public void initNode(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.init(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.init(clusterProperties, nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'install' command on specified node(s)", key = {"install"})
    public void installNode(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.install(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.install(clusterProperties, nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Run 'sql' command on local node", key = {"sql"})
    public void sqlNode(
            @ShellOption(help = "Node ID (1-based)", defaultValue = DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());

        if (all) {
            clusterProperties.getNodes().forEach(nodeProperties ->
                    clusterOperator.install(clusterProperties, nodeProperties.getId()));
        } else {
            clusterOperator.sqlNode(clusterProperties, nodeId != null ? nodeId : NODE_ID_SELECTION.get());
        }
    }
}

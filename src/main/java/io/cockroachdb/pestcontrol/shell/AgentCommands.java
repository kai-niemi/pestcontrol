package io.cockroachdb.pestcontrol.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pestcontrol.cluster.ClusterManager;
import io.cockroachdb.pestcontrol.cluster.ClusterOperator;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.shell.client.HypermediaClient;
import io.cockroachdb.pestcontrol.shell.support.ListTableModel;
import io.cockroachdb.pestcontrol.shell.support.TableUtils;
import static io.cockroachdb.pestcontrol.api.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pestcontrol.api.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.AGENT_COMMANDS)
public class AgentCommands {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private AnsiConsole ansiConsole;

    @ShellMethod(value = "List agent information", key = {"list"})
    public void listNodes(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId) {

        ansiConsole.yellow("Local build: %s v%s", buildProperties.getName(), buildProperties.getVersion()).nl();

        ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(clusterId);
        if (clusterProperties.getNodes().isEmpty()) {
            ansiConsole.yellow("No configured nodes for cluster id: %s", clusterId).nl();
        }

        List<List<?>> tuples = new ArrayList<>();

        clusterProperties.getNodes()
                .forEach(nodeProperties -> {
                    try {
                        ansiConsole.yellow("Querying: %s ..", nodeProperties.getBaseUrl().toString()).nl();

                        Map<String, Object> build = hypermediaClient.from(nodeProperties.getBaseUrl())
                                .follow(curied(CURIE_NAMESPACE, ACTUATORS_REL).value())
                                .follow(HalLinkRelation.uncuried("info").value())
                                .toObject("$.build");

                        Object name = build.getOrDefault("name", "n/a");
                        Object version = build.getOrDefault("version", "n/a");

                        tuples.add(List.of(nodeProperties.getUrl(), name, version,
                                version.equals(buildProperties.getVersion()) ? "" : "Version divergence!"));
                    } catch (ResourceAccessException e) {
                        tuples.add(List.of(nodeProperties.getUrl(), "??", "??", e.getMessage()));
                    }
                });

        AtomicInteger idx = new AtomicInteger();

        ansiConsole.yellow(TableUtils.prettyPrint(
                        new ListTableModel<>(tuples,
                                List.of("#", "URL", "Name", "Version", "Note"),
                                (object, column) -> switch (column) {
                                    case 0 -> idx.incrementAndGet();
                                    case 1 -> object.get(0);
                                    case 2 -> object.get(1);
                                    case 3 -> object.get(2);
                                    case 4 -> object.get(3);
                                    default -> "??";
                                })))
                .nl();
    }

    @ShellMethod(value = "Run 'start' command on specified agent node(s)", key = {"start"})
    public void agentStart(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId,
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

    @ShellMethod(value = "Run 'stop' command on specified agent node(s)", key = {"stop"})
    public void agentStop(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId,
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

    @ShellMethod(value = "Run 'kill' command on specified agent node(s)", key = {"kill"})
    public void agentKill(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId,
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

    @ShellMethod(value = "Run 'init' command on specified agent node(s)", key = {"init"})
    public void agentInit(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId,
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

    @ShellMethod(value = "Run 'install' command on specified agent node(s)", key = {"install"})
    public void agentInstall(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId,
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

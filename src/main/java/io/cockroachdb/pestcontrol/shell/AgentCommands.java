package io.cockroachdb.pestcontrol.shell;

import java.util.ArrayList;
import java.util.EnumSet;
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
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.cluster.ClusterOperator;
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

    @ShellMethod(value = "List agent information", key = {"agent-list", "al"})
    public void listNodes(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId) {

        ansiConsole.yellow("Local build: %s v%s", buildProperties.getName(), buildProperties.getVersion()).nl();

        ClusterProperties clusterProperties = applicationProperties
                .getClusterPropertiesById(clusterId,
                        EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure,
                                ClusterType.remote_insecure, ClusterType.remote_secure));

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

    @ShellMethod(value = "Run 'start' command on a specified agent", key = {"agent-start", "at"})
    public void agentStart(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.startNode(clusterProperties, nodeId);
    }

    @ShellMethod(value = "Run 'stop' command on a specified agent", key = {"agent-stop", "ap"})
    public void agentStop(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.stopNode(clusterProperties, nodeId);
    }

    @ShellMethod(value = "Run 'kill' command on a specified agent", key = {"agent-kill", "ak"})
    public void agentKill(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.killNode(clusterProperties, nodeId);
    }

    @ShellMethod(value = "Run 'init' command on a specified agent", key = {"agent-init", "ai"})
    public void agentInit(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.init(clusterProperties, nodeId);
    }

    @ShellMethod(value = "Run 'install' command on a specified agent", key = {"agent-install", "al"})
    public void agentInstall(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    valueProvider = ClusterProvider.class) String clusterId,
            @ShellOption(help = "Node ID (1-based)") int nodeId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.install(clusterProperties, nodeId);
    }
}

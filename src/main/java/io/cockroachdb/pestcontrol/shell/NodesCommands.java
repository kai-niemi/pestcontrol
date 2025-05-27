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

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.manager.ClusterManager;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.operator.ClusterOperator;
import io.cockroachdb.pestcontrol.shell.support.AnsiConsole;
import io.cockroachdb.pestcontrol.shell.support.HypermediaClient;
import io.cockroachdb.pestcontrol.shell.support.ListTableModel;
import io.cockroachdb.pestcontrol.shell.support.NodeProvider;
import io.cockroachdb.pestcontrol.shell.support.TableUtils;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.AGENT_COMMANDS)
public class NodesCommands {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AnsiConsole ansiConsole;

    @ShellMethod(value = "List agent information", key = {"node-list", "nl"})
    public void listNodes(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    defaultValue = "Remote Insecure Cluster") String clusterId) {

        ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(clusterId);

        List<List<?>> tuples = new ArrayList<>();

        clusterProperties.getMachines()
                .forEach(nodeProperties -> {
                    try {
                        Map<String, Object> build = hypermediaClient.from(nodeProperties.getBaseUrl())
                                .follow(curied(LinkRelations.CURIE_NAMESPACE, LinkRelations.ACTUATORS_REL).value())
                                .follow(HalLinkRelation.uncuried("info").value())
                                .toObject("$.build");

                        Object name = build.getOrDefault("name", "n/a");
                        Object version = build.getOrDefault("version", "n/a");

                        tuples.add(List.of(nodeProperties.getUrl(), name, version,
                                version.equals(
                                        buildProperties.getVersion()) ? "Version convergence" : "Version divergence"));
                    } catch (ResourceAccessException e) {
                        tuples.add(List.of(nodeProperties.getUrl(), "??", "??",
                                e.getMessage()));
                    }
                });

        AtomicInteger idx = new AtomicInteger();

        ansiConsole.yellow("Local build: %s v%s", buildProperties.getName(), buildProperties.getVersion()).nl();

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

    @ShellMethod(value = "Run 'start' command on a specified node", key = {"node-start", "ns"})
    public void startNode(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    defaultValue = "Remote Insecure Cluster") String clusterId,
            @ShellOption(help = "Node ID", valueProvider = NodeProvider.class, defaultValue = "1") int nodeId) {

        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.startNode(clusterProperties, nodeId);
    }

    @ShellMethod(value = "Run 'stop' command on a specified node", key = {"node-stop", "np"})
    public void stopNode(
            @ShellOption(help = "Remote cluster ID to use (must be remote cluster type)",
                    defaultValue = "Remote Insecure Cluster") String clusterId,
            @ShellOption(help = "Node ID", valueProvider = NodeProvider.class, defaultValue = "1") int nodeId) {

        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.stopNode(clusterProperties, nodeId);
    }
}

package io.cockroachdb.pest.shell;

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
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.NodeProperties;
import io.cockroachdb.pest.shell.client.HypermediaClient;
import io.cockroachdb.pest.shell.support.AnsiConsole;
import io.cockroachdb.pest.shell.support.ClusterProvider;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import static io.cockroachdb.pest.api.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pest.api.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.CLUSTER_COMMANDS)
public class ReportingCommands {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AnsiConsole ansiConsole;

    @ShellMethod(value = "List cluster endpoint information (pestcontrol instances)", key = {"list"})
    public void listHosts(
            @ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
                    valueProvider = ClusterProvider.class, defaultValue = "hosted-insecure") String clusterId) {

        List<String> ids = new ArrayList<>();
        if (StringUtils.hasLength(clusterId)) {
            ids.add(clusterId);
        } else {
            ids.addAll(applicationProperties.getClusterIds(
                    EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure)));
        }

        ids.forEach(id -> {
            ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(clusterId);
            if (clusterProperties.getNodes().isEmpty()) {
                ansiConsole.yellow("No configured nodes for cluster id: %s", clusterId).nl();
            } else {
                ansiConsole.yellow("Cluster id: %s name: %s type: %s", clusterProperties.getClusterId(),
                        clusterProperties.getClusterName(),
                        clusterProperties.getClusterType()
                        ).nl();
                clusterProperties.getNodes().forEach(this::printNode);
            }
        });
    }

    private void printNode(NodeProperties nodeProperties) {
        List<List<?>> tuples = new ArrayList<>();

        try {
            ansiConsole.yellow("Node %s (%s):", nodeProperties.getName(), nodeProperties.getBaseUrl()).nl();

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
}

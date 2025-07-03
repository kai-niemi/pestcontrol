package io.cockroachdb.pest.shell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.NodeProperties;
import io.cockroachdb.pest.shell.client.HypermediaClient;
import io.cockroachdb.pest.shell.support.ClusterProvider;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import io.cockroachdb.pest.util.Networking;
import static io.cockroachdb.pest.api.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pest.api.LinkRelations.CURIE_NAMESPACE;
import static io.cockroachdb.pest.shell.ClusterCommands.CLUSTER_SELECTION;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.REPORTING_COMMANDS)
public class ReportingCommands {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Value("${server.port:8080}")
    private Integer serverPort;

    public Availability ifClusterSelected() {
        return Objects.isNull(CLUSTER_SELECTION.get())
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Print cluster IP addresses and admin URLs", key = {"cluster-ip", "ip"})
    public void ip(
            @ShellOption(help = "Node ID (1-based)", defaultValue = ClusterCommands.DEFAULT_NODE_ID) Integer nodeId,
            @ShellOption(help = "Include all nodes", defaultValue = "false") Boolean all) {
        ClusterProperties clusterProperties = CLUSTER_SELECTION.get();

        List<NodeProperties> nodes = new ArrayList<>();
        if (all) {
            nodes.addAll(clusterProperties.getNodes());
        } else {
            nodes.add(clusterProperties.getNodes()
                    .stream()
                    .filter(x -> x.getId().equals(nodeId))
                    .findFirst().orElseThrow());
        }

        List<List<?>> tuples = new ArrayList<>();

        nodes.forEach(nodeProperties -> {
            tuples.add(List.of(
                    nodeProperties.getId(),
                    "%s//%s".formatted(nodeProperties.getBaseUrl(),
                            "%s//%s".formatted(nodeProperties.isSecure() ? "https:" : "http",
                                    nodeProperties.getHttpAddr()),
                            nodeProperties.getSqlAddr()
                    )));
        });

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Id", "Base URL", "Admin URL", "SQL Addr"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            case 3 -> object.get(3);
                            default -> "??";
                        }));

        logger.info("\n%s".formatted(table));
    }

    @ShellMethod(value = "Print local IP addresses and admin URLs", key = "local-ip")
    public void localIP() {
        logger.info("Local IP: %s".formatted(Networking.getLocalIP()));
        logger.info("External IP: %s".formatted(Networking.getExternalIP()));
        logger.info("Hostname: %s".formatted(Networking.getHostname()));
        logger.info("Local admin URL: http://%s:%d".formatted(Networking.getLocalIP(), serverPort));
        logger.info("External admin URL: http://%s:%d".formatted(Networking.getExternalIP(), serverPort));
    }

    @ShellMethod(value = "Print cluster configuration(s)", key = {"config"})
    public void printConfig(@ShellOption(help = "Cluster ID to use (all of hosted type if empty)",
            valueProvider = ClusterProvider.class, defaultValue = ShellOption.NULL)
                            String clusterId) {
        List<ClusterProperties> clusterPropertiesList = new ArrayList<>();
        if (StringUtils.hasLength(clusterId)) {
            clusterPropertiesList.add(applicationProperties.getClusterPropertiesById(clusterId));
        } else {
            applicationProperties.getClusterIds(
                            EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure))
                    .forEach(id -> {
                        clusterPropertiesList.add(applicationProperties.getClusterPropertiesById(id));
                    });
        }

        List<List<?>> tuples = new ArrayList<>();

        clusterPropertiesList.forEach(clusterProperties -> {
            tuples.add(List.of(
                    clusterProperties.getClusterId(),
                    clusterProperties.getClusterName(),
                    clusterProperties.getClusterType(),
                    Objects.requireNonNullElse(clusterProperties.getVersion(), "(n/a)")
            ));
        });

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Id", "Name", "Type", "Version"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            case 3 -> object.get(3);
                            default -> "??";
                        }));

        logger.info("\n%s".formatted(table));
    }

    @ShellMethod(value = "Ping cluster endpoints and report version", key = {"ping"})
    public void ping(
            @ShellOption(help = "Cluster ID to use (all of hosted type if empty)",
                    valueProvider = ClusterProvider.class, defaultValue = ClusterCommands.DEFAULT_CLUSTER_ID)
            String clusterId) {

        List<ClusterProperties> clusterPropertiesList = new ArrayList<>();
        if (StringUtils.hasLength(clusterId)) {
            clusterPropertiesList.add(applicationProperties.getClusterPropertiesById(clusterId));
        } else {
            applicationProperties.getClusterIds(
                            EnumSet.of(ClusterType.hosted_insecure, ClusterType.hosted_secure))
                    .forEach(id ->
                            clusterPropertiesList.add(applicationProperties.getClusterPropertiesById(id)));
        }

        List<List<?>> tuples = new ArrayList<>();

        clusterPropertiesList.forEach(clusterProperties -> {
            logger.info("%s (%s)".formatted(clusterProperties.getClusterId(),
                    clusterProperties.getClusterName()));

            clusterProperties.getNodes().forEach(nodeProperties -> {
                try {
                    Map<String, Object> build = hypermediaClient.from(nodeProperties.getBaseUrl())
                            .follow(curied(CURIE_NAMESPACE, ACTUATORS_REL).value())
                            .follow(HalLinkRelation.uncuried("info").value())
                            .toObject("$.build");

                    Object name = build.getOrDefault("name", "n/a");
                    Object version = build.getOrDefault("version", "n/a");

                    tuples.add(List.of(
                            nodeProperties.getId(),
                            nodeProperties.getName(),
                            nodeProperties.getUrl(),
                            name,
                            version,
                            version.equals(buildProperties.getVersion()) ? "" : "Version divergence!"));
                } catch (ResourceAccessException e) {
                    tuples.add(List.of(nodeProperties.getUrl(), "??", "??", e.getMessage()));
                }
            });
        });

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Id", "Name", "URL", "API Name", "API Version", "API Note"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            case 3 -> object.get(3);
                            case 4 -> object.get(4);
                            default -> "??";
                        }));

        logger.info("\n%s".formatted(table));
    }
}

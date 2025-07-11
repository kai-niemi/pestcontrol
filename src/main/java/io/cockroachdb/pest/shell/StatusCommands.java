package io.cockroachdb.pest.shell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.shell.client.HypermediaClient;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import io.cockroachdb.pest.util.Networking;
import static io.cockroachdb.pest.api.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pest.api.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.STATUS_COMMANDS)
public class StatusCommands extends AbstractCommand {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private BuildProperties buildProperties;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Print cluster IP addresses and admin URLs", key = {"ip"})
    public void clusterIP() {
        logger.info("Local IP: %s".formatted(Networking.getLocalIP()));
        logger.info("External IP: %s".formatted(Networking.getExternalIP()));
        logger.info("Hostname: %s".formatted(Networking.getHostname()));
        logger.info("Local admin URL: http://%s:%d".formatted(Networking.getLocalIP(), serverPort));
        logger.info("External admin URL: http://%s:%d".formatted(Networking.getExternalIP(), serverPort));

        List<List<?>> tuples = new ArrayList<>();

        getClusterProperties()
                .getNodes().forEach(nodeProperties ->
                        tuples.add(List.of(Objects.requireNonNull(nodeProperties.getId()),
                                nodeProperties.getBaseUrl().getHref(),
                                "http%s//%s".formatted(nodeProperties.isSecure() ? "s:" : ":",
                                        Objects.requireNonNull(nodeProperties.getHttpAddr())),
                                Objects.requireNonNullElse(nodeProperties.getSqlAddr(), "n/a"))
                        ));

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Id", "API URL", "Admin URL", "SQL Addr"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            case 3 -> object.get(3);
                            default -> "??";
                        }));

        logger.info("Nodes:\n%s".formatted(table));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Print cluster configuration(s)", key = {"config"})
    public void printConfig() {
        List<List<?>> tuples = new ArrayList<>();

        applicationProperties.getClusters()
                .forEach(clusterProperties -> {
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

        logger.info("Clusters:\n%s".formatted(table));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Ping cluster endpoints and report version", key = {"ping"})
    public void ping() {
        List<List<?>> tuples = new ArrayList<>();

        getClusterProperties().getNodes().forEach(nodeProperties -> {
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
        logger.info("Endpoints:\n%s".formatted(table));
    }
}

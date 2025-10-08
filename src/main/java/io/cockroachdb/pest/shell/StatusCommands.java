package io.cockroachdb.pest.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.util.HypermediaClient;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import io.cockroachdb.pest.util.Networking;
import static io.cockroachdb.pest.web.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pest.web.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.STATUS_COMMANDS)
public class StatusCommands extends AbstractCommand {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Print cluster IP addresses and admin URLs", key = {"ip"})
    public void ip() {
        logger.info("""
                
                            Local IP: %s
                         External IP: %s
                            Hostname: %s
                Hostname (canonical): %s
                     Local admin URL: %s
                  External admin URL: %s""".formatted(
                Networking.getLocalIP(),
                Networking.getExternalIP(),
                Networking.getHostname(),
                Networking.getCanonicalHostName(),
                "http://%s:%d".formatted(Networking.getLocalIP(), serverPort),
                "http://%s:%d".formatted(Networking.getExternalIP(), serverPort)
        ));
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Print cluster agents", key = {"agents", "a"})
    public void printAgents() {
        List<List<?>> tuples = new ArrayList<>();

        Cluster cluster = getClusterProperties();

        cluster.getNodes().forEach(node -> {
            try {
                Map<String, Object> build = hypermediaClient.from(node
                                .getServiceLink())
                        .follow(curied(CURIE_NAMESPACE, ACTUATORS_REL).value())
                        .follow(HalLinkRelation.uncuried("info").value())
                        .toObject("$.build");

                Object name = build.getOrDefault("name", "n/a");
                Object version = build.getOrDefault("version", "n/a");

                tuples.add(List.of(
                        node.getId(),
                        node.getName(),
                        node.getServiceLink().getHref(),
                        name,
                        version
                ));
            } catch (ResourceAccessException e) {
                tuples.add(List.of(node.getServiceLink().getHref(), "??", "??", e.getMessage()));
            }
        });

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Id", "Name", "URL", "API Name", "API Version"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            case 3 -> object.get(3);
                            default -> "??";
                        }));
        logger.info("%n%s".formatted(table));
    }
}

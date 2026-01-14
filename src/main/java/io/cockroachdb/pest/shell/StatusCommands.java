package io.cockroachdb.pest.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import io.cockroachdb.pest.util.HypermediaClient;
import io.cockroachdb.pest.util.NetworkAddress;
import static io.cockroachdb.pest.web.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pest.web.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@Component
public class StatusCommands extends AbstractShellCommand {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Command(description = "Print local IP addresses",
            name = {"status", "ip"},
            group = CommandGroups.STATUS_COMMANDS,
            availabilityProvider = "ifClusterSelected")
    public void printIP(CommandContext commandContext) {
        System.out.println(
                """
                        
                                    Local IP: %s
                                 External IP: %s
                                    Hostname: %s
                        Hostname (canonical): %s
                              Local API root: %s
                           External API root: %s""".formatted(
                        NetworkAddress.getLocalIP(),
                        NetworkAddress.getExternalIP(),
                        NetworkAddress.getHostname(),
                        NetworkAddress.getCanonicalHostName(),
                        "http://%s:%d".formatted(NetworkAddress.getLocalIP(), serverPort),
                        "http://%s:%d".formatted(NetworkAddress.getExternalIP(), serverPort)
                ));
    }

    @Command(description = "Print pest control agents",
            name = {"status", "agents"},
            group = CommandGroups.STATUS_COMMANDS,
            availabilityProvider = "ifClusterSelected")
    public void printAgents(CommandContext commandContext) {
        List<List<?>> tuples = new ArrayList<>();

        Cluster cluster = selectedCluster();

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
        System.out.println("%n%s".formatted(table));
    }
}

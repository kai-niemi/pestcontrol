package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.completion.CompletionProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

import jakarta.annotation.PostConstruct;

import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.NetworkAddress;
import io.cockroachdb.pest.shell.support.ClusterCompletionProvider;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import io.cockroachdb.pest.util.HypermediaClient;
import static io.cockroachdb.pest.model.LinkRelations.ACTUATORS_REL;
import static io.cockroachdb.pest.model.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@Component
public class ClusterCommands extends AbstractShellCommand {
    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Autowired
    private HypermediaClient hypermediaClient;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @PostConstruct
    public void init() {
        if (StringUtils.hasLength(applicationProperties.getDefaultClusterId())) {
            useCluster(applicationProperties.getDefaultClusterId());
        } else {
            useCluster(applicationProperties.getClusterIds().stream().findFirst().orElseThrow());
        }
    }

    @Bean
    public CompletionProvider clusterCompletionProvider() {
        return new ClusterCompletionProvider(applicationProperties);
    }

    @Command(description = "Select default cluster ID to use in commands",
            name = {"use", "cluster"},
            group = CommandGroups.CLUSTER_COMMANDS,
            completionProvider = "clusterCompletionProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void useCluster(
            @Argument(description = "Cluster ID to use (must be of hosted cluster type)", index = 0)
            String clusterId) {
        if (!Objects.equals("none", clusterId)) {
            SELECTED_CLUSTER = applicationProperties.getClusterById(clusterId);
        } else {
            SELECTED_CLUSTER = null;
        }
    }

    @Command(description = "Display database version",
            name = {"show", "database-version"},
            alias = "dbv",
            group = CommandGroups.CLUSTER_COMMANDS,
            availabilityProvider = "ifClusterSelected",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void showDatabaseVersion(CommandContext commandContext) {
        try {
            Cluster cluster = selectedCluster();
            String version = clusterOperatorProvider
                    .clusterOperator(cluster.getClusterId())
                    .statusOperator(cluster)
                    .clusterVersion();
            commandContext.outputWriter().println(version);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Command(description = "Display local IP addresses",
            name = {"show", "ip"},
            group = CommandGroups.CLUSTER_COMMANDS,
            availabilityProvider = "ifClusterSelected",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void printIP(CommandContext commandContext) {
        commandContext.outputWriter().println(
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

    @Command(description = "Display pest control agents",
            name = {"show", "agents"},
            group = CommandGroups.CLUSTER_COMMANDS,
            availabilityProvider = "ifClusterSelected",
            exitStatusExceptionMapper = "commandExceptionMapper")
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

        commandContext.outputWriter().printf("%n%s%n", table);
    }
}

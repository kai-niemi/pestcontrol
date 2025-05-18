package io.cockroachdb.pestcontrol.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.ResourceAccessException;

import io.cockroachdb.pestcontrol.model.AgentProperties;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.shell.support.AgentProvider;
import io.cockroachdb.pestcontrol.shell.support.AnsiConsole;
import io.cockroachdb.pestcontrol.shell.support.HypermediaClient;
import io.cockroachdb.pestcontrol.shell.support.ListTableModel;
import io.cockroachdb.pestcontrol.shell.support.TableUtils;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@ShellComponent
@ShellCommandGroup(Constants.AGENT_COMMANDS)
public class AgentCommands {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AnsiConsole ansiConsole;

    public Availability ifAgents() {
        return applicationProperties.getAgents().isEmpty()
                ? Availability.unavailable("no agents are configured!")
                : Availability.available();
    }

    private static Link apiRootFor(AgentProperties agent) {
        String path = (agent.getUrl().endsWith("/") ? "api" : "/api");
        return Link.of(agent.getUrl() + path);
    }

    private Stream<AgentProperties> findAgents(int id) {
        return id > 0
                ? applicationProperties.getAgents()
                .stream()
                .filter(agent -> agent.getId() == id)
                .limit(1)
                : applicationProperties.getAgents().stream();
    }

    @ShellMethodAvailability("ifAgents")
    @ShellMethod(value = "List agent information", key = {"agent-list", "al"})
    public void listAgents(@ShellOption(value = {"id"},
            help = "Find agent by ID if > 0 or include all",
            valueProvider = AgentProvider.class, defaultValue = "0") int id) {
        List<List<?>> tuples = new ArrayList<>();

        findAgents(id)
                .forEach(agent -> {
                    try {
                        Map<String, Object> build = hypermediaClient.from(apiRootFor(agent))
                                .follow(curied(LinkRelations.CURIE_NAMESPACE, LinkRelations.ACTUATORS_REL).value())
                                .follow(HalLinkRelation.uncuried("info").value())
                                .toObject("$.build");

                        Object name = build.getOrDefault("name", "n/a");
                        Object version = build.getOrDefault("version", "n/a");

                        tuples.add(List.of(agent.getUrl(), name, version,
                                version.equals(
                                        buildProperties.getVersion()) ? "Version convergence" : "Version divergence"));
                    } catch (ResourceAccessException e) {
                        tuples.add(List.of(agent.getUrl(), "??", "??",
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

    @ShellMethodAvailability("ifAgents")
    @ShellMethod(value = "Run 'start' command on specified agent(s)", key = {"agent-start", "as"})
    public void startAgents(@ShellOption(value = {"id"},
            help = "Find agent by ID if > 0 or include all",
            valueProvider = AgentProvider.class, defaultValue = "0") int id) {

        findAgents(id).forEach(agentProperties -> {
        });
    }
}

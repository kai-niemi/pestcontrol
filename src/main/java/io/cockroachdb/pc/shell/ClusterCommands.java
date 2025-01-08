package io.cockroachdb.pc.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pc.model.ApplicationProperties;
import io.cockroachdb.pc.shell.support.AnsiConsole;
import io.cockroachdb.pc.shell.support.ListTableModel;
import io.cockroachdb.pc.shell.support.TableUtils;

@ShellComponent
@ShellCommandGroup(Constants.CLUSTER_COMMANDS)
public class ClusterCommands {
    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AnsiConsole ansiConsole;

    @ShellMethod(value = "List pest control agent info", key = {"agents", "a"})
    public void agents() {
        List<List<?>> tuples = new ArrayList<>();

        applicationProperties.getAgents().forEach(agentProperties -> {
            try {
                ResponseEntity<String> responseEntity = RestClient.builder()
                        .build()
                        .get()
                        .uri(agentProperties.getUrl() + "/actuator/info")
                        .retrieve()
                        .toEntity(String.class);

                Map build = JsonPath.parse(responseEntity.getBody()).read("$.build", Map.class);

                Object name = build.getOrDefault("name", "n/a");
                Object version = build.getOrDefault("version", "n/a");

                tuples.add(List.of(agentProperties.getUrl(),
                        agentProperties.getName(),
                        name,
                        version,
                        responseEntity.getStatusCode(),
                        version.equals(buildProperties.getVersion())
                                ? "Version convergence" : "Version divergence"
                ));
            } catch (ResourceAccessException e) {
                tuples.add(List.of(agentProperties.getUrl(),
                        agentProperties.getName(),
                        "??",
                        "??",
                        e.getMessage(),
                        "Unknown"
                ));
            }
        });

        AtomicInteger idx = new AtomicInteger();

        ansiConsole.yellow("Local build: %s v%s", buildProperties.getName(), buildProperties.getVersion()).nl();

        ansiConsole.yellow(TableUtils.prettyPrint(
                        new ListTableModel<>(tuples,
                                List.of("#", "URL", "Name", "ID", "Version", "Status", "Remark"),
                                (object, column) -> switch (column) {
                                    case 0 -> idx.incrementAndGet();
                                    case 1 -> object.get(0);
                                    case 2 -> object.get(1);
                                    case 3 -> object.get(2);
                                    case 4 -> object.get(3);
                                    case 5 -> object.get(4);
                                    case 6 -> object.get(5);
                                    default -> "??";
                                })))
                .nl();
    }
}

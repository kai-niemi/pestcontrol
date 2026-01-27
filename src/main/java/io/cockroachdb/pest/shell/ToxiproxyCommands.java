package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.core.command.availability.Availability;
import org.springframework.shell.core.command.completion.CompletionProvider;
import org.springframework.shell.core.command.completion.CompositeCompletionProvider;
import org.springframework.shell.core.command.completion.EnumCompletionProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;

import io.cockroachdb.pest.cluster.ResourceNotFoundException;
import io.cockroachdb.pest.cluster.local.CommandBuilder;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.Node;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;

@Component
public class ToxiproxyCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ApplicationProperties applicationProperties;

    public Availability toxiProxyServerProvider() {
        try {
            applicationProperties.createToxiProxyClient().version();
            return Availability.available();
        } catch (IOException e) {
            return Availability.unavailable("toxiproxy is not installed or not running (" + e.getMessage() + ")!");
        }
    }

    @Command(description = "Start toxiproxy server on local host",
            name = {"toxi", "start", "server"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "ifClusterSelected",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void startToxiproxyServer() throws IOException {
        CommandBuilder.builder()
                .withBaseDir(applicationProperties.getDirectories().getBaseDirPath())
                .withCommand("start-toxiproxy")
                .withFlags("--toxiproxy-host=" + applicationProperties.getToxiProxy().getHost())
                .withFlags("--toxiproxy-port=" + applicationProperties.getToxiProxy().getPort())
                .execute();
    }

    @Command(description = "Stop toxiproxy server on local host",
            name = {"toxi", "stop", "server"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "ifClusterSelected",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void stopToxiproxyServer() throws IOException {
        CommandBuilder.builder()
                .withBaseDir(applicationProperties.getDirectories().getBaseDirPath())
                .withCommand("stop-toxiproxy")
                .withFlags("--toxiproxy-host=" + applicationProperties.getToxiProxy().getHost())
                .withFlags("--toxiproxy-port=" + applicationProperties.getToxiProxy().getPort())
                .execute();
    }

    @Command(description = "Print toxiproxy server version",
            name = {"toxi", "server", "version"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void proxyVersion(CommandContext commandContext) throws IOException {
        commandContext.outputWriter().println(applicationProperties.createToxiProxyClient().version());
    }

    @Command(description = "Reset toxiproxy server on this host",
            name = {"toxi", "reset", "server"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void resetProxy() throws IOException {
        applicationProperties.createToxiProxyClient().reset();
    }

    @Command(description = "List all toxiproxy proxies",
            name = {"toxi", "list", "proxy"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void listProxies(CommandContext commandContext) throws IOException {
        List<List<?>> tuples = new ArrayList<>();

        applicationProperties.createToxiProxyClient()
                .getProxies().forEach(proxy -> {
                    tuples.add(List.of(
                            proxy.getName(),
                            proxy.getListen(),
                            proxy.getUpstream()
                    ));
                });

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Name", "Listen", "Upstream"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            default -> "??";
                        }));

        commandContext.outputWriter().println("Proxies:\n%s".formatted(table));
    }

    @Command(description = "Create toxiproxy proxy on specified nodes(s)",
            name = {"toxi", "create", "proxy"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void createProxy(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id,
            CommandContext commandContext) {
        Cluster cluster = selectedCluster();

        nodeIdRange(id).forEach(nodeId -> {
            Node node = cluster.getNodeById(nodeId);

            try {
                Proxy proxy = applicationProperties.createToxiProxyClient()
                        .createProxy(node.getName(),
                                Objects.requireNonNull(node.getAdvertiseProxyAddr()),
                                Objects.requireNonNull(node.getListenAddr()));
                commandContext.outputWriter().println("Added %s with listen addr %s upstream addr %s"
                        .formatted(proxy.getName(), proxy.getListen(), proxy.getUpstream()));
            } catch (IOException e) {
                throw new UncheckedIOException("I/O exception in toxiproxy client", e);
            }
        });
    }

    @Command(description = "Delete toxiproxy proxy for specified nodes(s)",
            name = {"toxi", "delete", "proxy"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void deleteProxy(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id,
            CommandContext commandContext) throws IOException {
        Cluster cluster = selectedCluster();

        for (Integer nodeId : nodeIdRange(id)) {
            Node node = cluster.getNodeById(nodeId);

            proxyByName(node.getName())
                    .ifPresent(proxy -> {
                        try {
                            proxy.delete();
                            commandContext.outputWriter().println("Deleted %s".formatted(proxy.getName()));
                        } catch (IOException e) {
                            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
                        }
                    });
        }
    }

    @Command(description = "Enable proxy for specified nodes(s)",
            name = {"toxi", "enable", "proxy"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void enableProxy(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id,
            CommandContext commandContext) throws IOException {
        Cluster cluster = selectedCluster();

        for (Integer nodeId : nodeIdRange(id)) {
            Node node = cluster.getNodeById(nodeId);

            proxyByName(node.getName())
                    .ifPresent(proxy -> {
                        try {
                            proxy.enable();
                            commandContext.outputWriter().println("Enabled %s".formatted(proxy.getName()));
                        } catch (IOException e) {
                            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
                        }
                    });
        }
    }

    @Command(description = "List all toxiproxy proxy toxics",
            name = {"toxi", "proxy", "list", "toxic"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void listProxyToxics(CommandContext commandContext) throws IOException {
        List<List<?>> tuples = new ArrayList<>();

        for (Proxy proxy : applicationProperties.createToxiProxyClient()
                .getProxies()) {
            proxy.toxics().getAll().forEach(toxic -> {
                tuples.add(List.of(
                        proxy.getName(),
                        toxic.getName(),
                        toxic.getToxicity(),
                        toxic.getStream().name()
                ));
            });
        }

        String table = TableUtils.prettyPrint(
                new ListTableModel<>(tuples,
                        List.of("Proxy", "Toxic", "Toxicity", "Direction"),
                        (object, column) -> switch (column) {
                            case 0 -> object.get(0);
                            case 1 -> object.get(1);
                            case 2 -> object.get(2);
                            case 3 -> object.get(3);
                            default -> "??";
                        }));

        commandContext.outputWriter().println("Proxy toxics:\n%s".formatted(table));
    }

    @Bean
    public CompletionProvider addToxicCompletionProvider() {
        EnumCompletionProvider a = new EnumCompletionProvider(ToxicType.class, "--toxicType");
        EnumCompletionProvider b = new EnumCompletionProvider(ToxicDirection.class, "--toxicDirection");
        return new CompositeCompletionProvider(a, b);
    }

    @Command(description = "Add proxy toxic",
            name = {"toxi", "proxy", "add", "toxic"},
            group = CommandGroups.TOXIPROXY_COMMANDS,
            availabilityProvider = "toxiProxyServerProvider",
            completionProvider = "addToxicCompletionProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void addToxic(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id,
            @Option(description = "Toxic type", defaultValue = "LATENCY", longName = "toxicType") ToxicType toxicType,
            @Option(description = "Toxic name", defaultValue = "latency-toxic", longName = "name") String name,
            @Option(description = "Link direction to affect", defaultValue = "DOWNSTREAM", longName = "direction")
            ToxicDirection direction,
            @Option(description = "Probability of the toxic being applied to a link (defaults to 1.0)", defaultValue = "1.0", longName = "toxicity")
            float toxicity,
            @Option(description = "Time in milliseconds (latency toxic)", defaultValue = "150", longName = "latency")
            long latency,
            @Option(description = "Time in milliseconds (latency toxic)", defaultValue = "150", longName = "jitter")
            long jitter,
            @Option(description = "Rate in KB/s (bandwidth toxic)", defaultValue = "33", longName = "rate") long rate,
            @Option(description = "time in microseconds to delay each packet by (slow_close and slicer toxics)", defaultValue = "100", longName = "delay")
            long delay,
            @Option(description = "Time in milliseconds (timeout and reset_peer toxics)", defaultValue = "1000", longName = "timeout")
            long timeout,
            @Option(description = "Size in bytes of an average packet (slicer toxic)", defaultValue = "1024", longName = "avgSize")
            long averageSize,
            @Option(description = "Variation in bytes of an average packet that should be smaller than average_size (slicer toxic)", defaultValue = "256", longName = "sizeVar")
            long sizeVariation,
            @Option(description = "Number of bytes it should transmit before connection is closed (limit_data toxic)", defaultValue = "8192", longName = "bytes")
            long bytes,
            CommandContext commandContext
    ) throws IOException {
        Cluster cluster = selectedCluster();
        Node node = cluster.getNodeById(Integer.parseInt(id));

        Proxy proxy = proxyByName(node.getName())
                .orElseThrow(() -> new ResourceNotFoundException("No such proxy: " + node.getName()));

        Toxic toxic = switch (toxicType) {
            case LATENCY -> proxy.toxics().latency(name, direction, latency);
            case BANDWIDTH -> proxy.toxics().bandwidth(name, direction, rate);
            case SLOW_CLOSE -> proxy.toxics().slowClose(name, direction, delay);
            case TIMEOUT -> proxy.toxics().timeout(name, direction, timeout);
            case SLICER -> proxy.toxics().slicer(name, direction, averageSize, delay)
                    .setSizeVariation(sizeVariation);
            case LIMIT_DATA -> proxy.toxics().limitData(name, direction, bytes);
            case RESET_PEER -> proxy.toxics().resetPeer(name, direction, timeout);
        };
        toxic.setToxicity(toxicity / 100.0f);

        commandContext.outputWriter().println("Added %s".formatted(toxic.getName()));
    }

    private Optional<Proxy> proxyByName(String name) throws IOException {
        Assert.notNull(name, "name is required");
        return applicationProperties
                .createToxiProxyClient()
                .getProxies()
                .stream()
                .filter(x -> name.equals(x.getName()))
                .findFirst();
    }
}

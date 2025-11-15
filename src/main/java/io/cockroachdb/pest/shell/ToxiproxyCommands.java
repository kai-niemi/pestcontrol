package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.Assert;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;

import io.cockroachdb.pest.cluster.ResourceNotFoundException;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;

@ShellComponent
@ShellCommandGroup(Constants.TOXIPROXY_COMMANDS)
public class ToxiproxyCommands extends AbstractCommand {
    @Autowired
    private ToxiproxyClient toxiproxyClient;

    public Availability ifToxiproxy() {
        try {
            toxiproxyClient.version();
            return Availability.available();
        } catch (IOException e) {
            return Availability.unavailable("toxiproxy is not installed or not running (" + e.getMessage() + ")!");
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start toxiproxy server on local host", key = {"start-toxiproxy", "sto"})
    public void startToxiproxyServer() {
        getClusterOperator(getSelectedCluster()).startToxiproxyServer();
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Stop toxiproxy server on local host", key = {"stop-toxiproxy", "pto"})
    public void stopToxiproxyServer() {
        getClusterOperator(getSelectedCluster()).stopToxiproxyServer();
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "Print toxiproxy server version", key = {"proxy-version"})
    public void proxyVersion() {
        try {
            logger.info(toxiproxyClient.version());
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
        }
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "Reset toxiproxy server on this host", key = {"reset-proxy"})
    public void resetProxy() {
        try {
            toxiproxyClient.reset();
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
        }
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "List all toxiproxy proxies", key = {"list-proxy"})
    public void listProxies() {
        try {
            List<List<?>> tuples = new ArrayList<>();

            toxiproxyClient.getProxies().forEach(proxy -> {
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

            logger.info("Proxies:\n%s".formatted(table));
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
        }
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "Create toxiproxy proxy for specified nodes(s)", key = {"create-proxy"})
    public void createProxy(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        Cluster cluster = getSelectedCluster();

        nodeIdRange(nodes).forEach(nodeId -> {
            Cluster.Node node = cluster.getNodeById(nodeId);

            try {
                Proxy proxy = toxiproxyClient.createProxy(node.getName(),
                        Objects.requireNonNull(node.getAdvertiseProxyAddr()),
                        Objects.requireNonNull(node.getListenAddr()));
                logger.info("Added %s with listen addr %s upstream addr %s"
                        .formatted(proxy.getName(), proxy.getListen(), proxy.getUpstream()));
            } catch (IOException e) {
                throw new UncheckedIOException("I/O exception in toxiproxy client", e);
            }
        });
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "Delete toxiproxy proxy for specified nodes(s)", key = {"delete-proxy"})
    public void deleteProxy(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        Cluster cluster = getSelectedCluster();

        nodeIdRange(nodes).forEach(nodeId -> {
            Cluster.Node node = cluster.getNodeById(nodeId);

            proxyByName(node.getName())
                    .ifPresent(proxy -> {
                        try {
                            proxy.delete();
                            logger.info("Deleted %s".formatted(proxy.getName()));
                        } catch (IOException e) {
                            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
                        }
                    });
        });
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "Enable proxy for specified nodes(s)", key = {"enable-proxy"})
    public void enableProxy(@ShellOption(help = "Node IDs range or 'all'") String nodes) {
        Cluster cluster = getSelectedCluster();

        nodeIdRange(nodes).forEach(nodeId -> {
            Cluster.Node node = cluster.getNodeById(nodeId);

            proxyByName(node.getName())
                    .ifPresent(proxy -> {
                        try {
                            proxy.enable();
                            logger.info("Enabled %s".formatted(proxy.getName()));
                        } catch (IOException e) {
                            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
                        }
                    });
        });
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "List all toxiproxy proxy toxics", key = {"list-toxics"})
    public void listProxyToxics() {
        try {
            List<List<?>> tuples = new ArrayList<>();

            for (Proxy proxy : toxiproxyClient.getProxies()) {
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

            logger.info("Proxy toxics:\n%s".formatted(table));
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
        }
    }

    @ShellMethodAvailability("ifToxiproxy")
    @ShellMethod(value = "Add proxy toxic", key = {"add-toxic"})
    public void addToxic(
            @ShellOption(help = "Node ID (1-based int)") String nodeId,
            @ShellOption(help = "Toxic type", defaultValue = "LATENCY", valueProvider = EnumValueProvider.class)
            ToxicType toxicType,
            @ShellOption(help = "Toxic name", defaultValue = "latency-toxic") String name,
            @ShellOption(help = "Link direction to affect", defaultValue = "DOWNSTREAM", valueProvider = EnumValueProvider.class)
            ToxicDirection direction,
            @ShellOption(help = "Probability of the toxic being applied to a link (defaults to 1.0)", defaultValue = "1.0")
            float toxicity,
            @ShellOption(help = "Time in milliseconds (latency toxic)", defaultValue = "150") long latency,
            @ShellOption(help = "Time in milliseconds (latency toxic)", defaultValue = "150") long jitter,
            @ShellOption(help = "Rate in KB/s (bandwidth toxic)", defaultValue = "33") long rate,
            @ShellOption(help = "time in microseconds to delay each packet by (slow_close and slicer toxics)", defaultValue = "100")
            long delay,
            @ShellOption(help = "Time in milliseconds (timeout and reset_peer toxics)", defaultValue = "1000")
            long timeout,
            @ShellOption(help = "Size in bytes of an average packet (slicer toxic)", defaultValue = "1024")
            long averageSize,
            @ShellOption(help = "Variation in bytes of an average packet that should be smaller than average_size (slicer toxic)", defaultValue = "256")
            long sizeVariation,
            @ShellOption(help = "Number of bytes it should transmit before connection is closed (limit_data toxic)", defaultValue = "8192")
            long bytes
    ) {
        Cluster cluster = getSelectedCluster();
        Cluster.Node node = cluster.getNodeById(Integer.parseInt(nodeId));

        try {
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

            logger.info("Added %s".formatted(toxic.getName()));
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in toxiproxy client", e);
        }
    }

    private Optional<Proxy> proxyByName(String name) {
        Assert.notNull(name, "name is required");
        try {
            return toxiproxyClient
                    .getProxies()
                    .stream()
                    .filter(x -> name.equals(x.getName()))
                    .findFirst();
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception retrieving proxies", e);
        }
    }
}

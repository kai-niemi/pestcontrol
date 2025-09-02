package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.ToxicType;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ResourceNotFoundException;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.NodeProperties;
import io.cockroachdb.pest.shell.support.ListTableModel;
import io.cockroachdb.pest.shell.support.TableUtils;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
@ShellCommandGroup(Constants.PROXY_COMMANDS)
public class ProxyCommands extends AbstractCommand {
    @Autowired
    private ToxiproxyClient toxiproxyClient;

    public Availability ifToxiProxyRunning() {
        if (ifClusterSelected().isAvailable()) {
            try {
                toxiproxyClient.version();
                return Availability.available();
            } catch (IOException e) {
                return Availability.unavailable("toxiproxy is not installed or not running (" + e.getMessage() + ")!");
            }
        } else {
            return ifClusterSelected();
        }
    }

    @ShellMethodAvailability("ifClusterSelected")
    @ShellMethod(value = "Start toxiproxy server on this host", key = {"start-proxy"})
    public void startProxy() {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.startProxyServer(clusterProperties);
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Stop toxiproxy server on this host", key = {"stop-proxy"})
    public void stopProxy() {
        ClusterProperties clusterProperties = getClusterProperties();
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
        clusterOperator.stopProxyServer(clusterProperties);
    }

//    @ShellMethodAvailability("ifToxiProxyRunning")
//    @ShellMethod(value = "Start toxiproxy client on specified node(s)", key = {"start-proxy-cli"})
//    public void startProxyCli(
//            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
//        ClusterProperties clusterProperties = getClusterProperties();
//        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterProperties.getClusterId());
//        PatternUtils.parseIntRange(nodes).forEach(id -> clusterOperator.startProxyClient(clusterProperties, id));
//    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Reset toxiproxy server on this host", key = {"reset-proxy"})
    public void resetProxy() {
        try {
            toxiproxyClient.reset();
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in client reset", e);
        }
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "List proxies", key = {"list-proxy"})
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
            throw new UncheckedIOException("I/O exception in client reset", e);
        }
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Add proxy for specified nodes(s)", key = {"add-proxy"})
    public void addProxy(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();

        PatternUtils.parseIntRange(nodes).forEach(nodeId -> {
            NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

            try {
                Proxy proxy = toxiproxyClient.createProxy(nodeProperties.getName(),
                        nodeProperties.getAdvertiseProxyAddr(),
                        nodeProperties.getListenAddr());
                logger.info("Added %s with listen addr %s upstream addr %s"
                        .formatted(proxy.getName(), proxy.getListen(), proxy.getUpstream()));
            } catch (IOException e) {
                throw new UncheckedIOException("I/O exception in client reset", e);
            }
        });
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Disable proxy for specified nodes(s)", key = {"disable-proxy"})
    public void disableProxy(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();

        PatternUtils.parseIntRange(nodes).forEach(nodeId -> {
            NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

            try {
                Proxy proxy = findProxyByName(nodeProperties.getName());
                proxy.disable();
                logger.info("Disabled %s".formatted(proxy.getName()));
            } catch (IOException e) {
                throw new UncheckedIOException("I/O exception in client reset", e);
            }
        });
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Enable proxy for specified nodes(s)", key = {"enable-proxy"})
    public void enableProxy(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String nodes) {
        ClusterProperties clusterProperties = getClusterProperties();

        PatternUtils.parseIntRange(nodes).forEach(nodeId -> {
            NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

            try {
                Proxy proxy = findProxyByName(nodeProperties.getName());
                proxy.enable();
                logger.info("Disabled %s".formatted(proxy.getName()));
            } catch (IOException e) {
                throw new UncheckedIOException("I/O exception in client reset", e);
            }
        });
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "List proxy toxics", key = {"list-toxics"})
    public void listToxics(
            @ShellOption(help = "Node IDs as comma separated list of 1-based ints and/or range") String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(Integer.parseInt(node));

        try {
            List<List<?>> tuples = new ArrayList<>();

            Proxy proxy = findProxyByName(nodeProperties.getName());
            proxy.toxics().getAll().forEach(toxic -> {
                tuples.add(List.of(
                        toxic.getName(),
                        toxic.getToxicity(),
                        toxic.getStream().name()
                ));
            });

            String table = TableUtils.prettyPrint(
                    new ListTableModel<>(tuples,
                            List.of("Name", "Toxicity", "Direction"),
                            (object, column) -> switch (column) {
                                case 0 -> object.get(0);
                                case 1 -> object.get(1);
                                case 2 -> object.get(2);
                                default -> "??";
                            }));

            logger.info("Proxies:\n%s".formatted(table));
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception in client reset", e);
        }
    }

    @ShellMethodAvailability("ifToxiProxyRunning")
    @ShellMethod(value = "Add proxy toxic", key = {"add-toxic"})
    public void addToxic(
            @ShellOption(help = "Node ID (1-based int)") String node,
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
        ClusterProperties clusterProperties = getClusterProperties();
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(Integer.parseInt(node));

        try {
            Proxy proxy = findProxyByName(nodeProperties.getName());
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
            throw new UncheckedIOException("I/O exception in client reset", e);
        }
    }

    private Proxy findProxyByName(String name) {
        try {
            return toxiproxyClient
                    .getProxies()
                    .stream()
                    .filter(x -> name.equals(x.getName())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No such proxy: " + name));
        } catch (IOException e) {
            throw new UncheckedIOException("I/O exception retrieving proxies", e);
        }
    }
}

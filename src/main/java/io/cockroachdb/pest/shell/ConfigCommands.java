package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;
import io.cockroachdb.pest.domain.Root;

@Component
public class ConfigCommands extends AbstractShellCommand {
    private static final char[] ALPHA = "abcdef".toCharArray();

    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Command(description = "Generate application YAML",
            name = {"admin", "config", "gen"},
            group = CommandGroups.ADMIN_COMMANDS,
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void generateConfig(
            @Option(description = "Name prefix", defaultValue = "cloud", longName = "name") String name,
            @Option(description = "Output file path", defaultValue = "", longName = "outputFile") String outputFile,
            @Option(description = "Region list", longName = "regions") List<String> regions,
            @Option(description = "Zone list", longName = "zones") List<String> zones,
            @Option(description = "Internal IP list", longName = "internalIPs") List<String> internalIPs,
            @Option(description = "Secure cluster", defaultValue = "false", longName = "secure") Boolean secure,
            CommandContext commandContext
    ) {
        Assert.isTrue(!regions.isEmpty(), "regions is empty");
        Assert.isTrue(!zones.isEmpty(), "zones is empty");
        Assert.state(regions.size() == zones.size(), "size of regions != size of zones");
        Assert.state(regions.size() == internalIPs.size(), "size of regions != size of ips");

        AddressCallback callback = new AddressCallback() {
            @Override
            public String firstNodeIP() {
                return internalIPs.getFirst();
            }

            private boolean isLocalIP(String ip) {
                return Objects.equals(ip, "localhost") || Objects.equals(ip, "127.0.0.1");
            }

            @Override
            public String serviceAddr(int id) {
                Assert.state(id > 0, "node id must be > 0");
                String ip = internalIPs.get(id - 1);
                id = isLocalIP(ip) ? 1 : id;
                return "http://%s:%d".formatted(ip, 9090 + id - 1);
            }

            @Override
            public String advertiseAddr(int id) {
                Assert.state(id > 0, "node id must be > 0");
                String ip = internalIPs.get(id - 1);
                id = isLocalIP(ip) ? 1 : id;
                return "%s:%d".formatted(ip, 26257 + id - 1);
            }

            @Override
            public String advertiseProxyAddr(int id) {
                Assert.state(id > 0, "node id must be > 0");
                String ip = internalIPs.get(id - 1);
                id = isLocalIP(ip) ? 1 : id;
                return "%s:%d".formatted(ip, 35257 + id - 1);
            }

            @Override
            public String listenAddr(int id) {
                Assert.state(id > 0, "node id must be > 0");
                String ip = internalIPs.get(id - 1);
                id = isLocalIP(ip) ? 1 : id;
                return "%s:%d".formatted(ip, 25257 + id - 1);
            }

            @Override
            public String sqlAddr(int id) {
                Assert.state(id > 0, "node id must be > 0");
                String ip = internalIPs.get(id - 1);
                id = isLocalIP(ip) ? 1 : id;
                return "%s:%d".formatted(ip, 26257 + id - 1);
            }

            @Override
            public String httpAddr(int id) {
                Assert.state(id > 0, "node id must be > 0");
                String ip = internalIPs.get(id - 1);
                id = isLocalIP(ip) ? 1 : id;
                return "%s:%d".formatted(ip, 8080 + id - 1);
            }
        };

        Cluster cluster = generateCluster(name, regions, zones, secure, callback);

        ApplicationProperties applicationProperties = new ApplicationProperties();
        applicationProperties.getClusters().add(cluster);
        applicationProperties.setDefaultClusterId(cluster.getClusterId());

        writeYaml(applicationProperties, yaml -> {
            if (Objects.isNull(outputFile)) {
                commandContext.outputWriter().println(yaml);
            } else {
                try {
                    Files.writeString(Path.of(outputFile), yaml,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE);
                    commandContext.outputWriter().println("Exported YAML to '%s'".formatted(outputFile));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
    }

    @Command(description = "Generate application YAML for localhost",
            name = {"admin", "config", "gen", "local"},
            group = CommandGroups.ADMIN_COMMANDS,
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void generateLocalConfig(
            @Option(description = "Name prefix", defaultValue = "cloud", longName = "name") String name,
            @Option(description = "Output file path", defaultValue = "", longName = "outputFile") String outputFile,
            @Option(description = "Regions without number suffix (like 'eu-central,eu-west,..')", defaultValue = "eu-central", longName = "regions")
            List<String> regions,
            @Option(description = "Number of zones per region (min 1)", defaultValue = "3", longName = "zones")
            int numZones,
            @Option(description = "Number of nodes per zone (min 1)", defaultValue = "1", longName = "nodes")
            int numNodes,
            @Option(description = "Secure cluster", defaultValue = "false", longName = "secure") Boolean secure,
            CommandContext commandContext
    ) {
        List<String> tiers = new ArrayList<>();
        List<String> zones = new ArrayList<>();
        List<String> internalIPs = new ArrayList<>();
        AtomicInteger regionNo = new AtomicInteger();

        regions.forEach(region -> {
            regionNo.incrementAndGet();
            IntStream.rangeClosed(1, numZones).forEach(zone -> {
                IntStream.rangeClosed(1, numNodes).forEach(node -> {
                    tiers.add(region + "-" + regionNo.get());
                    zones.add(region + "-" + regionNo.get() + ALPHA[zone - 1 % ALPHA.length]);
                    internalIPs.add("localhost");
                });
            });
        });

        generateConfig(name, outputFile, tiers, zones, internalIPs, secure, commandContext);
    }

    @Command(description = "Print application YAML",
            name = {"admin", "config", "print"},
            group = CommandGroups.ADMIN_COMMANDS,
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void printConfig(
            @Option(description = "Output file path", defaultValue = "", longName = "outputFile") String outputFile,
            CommandContext commandContext) {
        writeYaml(applicationProperties, yaml -> {
            if (StringUtils.hasLength(outputFile)) {
                try {
                    Files.writeString(Path.of(outputFile), yaml, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                    commandContext.outputWriter().println("Exported YAML to '%s'".formatted(outputFile));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                commandContext.outputWriter().println(yaml);
            }
        });
    }

    private void writeYaml(ApplicationProperties applicationProperties, Consumer<String> yaml) {
        try {
            StringWriter sw = new StringWriter();
            yamlObjectMapper.writerFor(Root.class).writeValue(sw, new Root(applicationProperties));
            yaml.accept(sw.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Cluster generateCluster(
            String name,
            List<String> regions,
            List<String> zones,
            boolean secure,
            AddressCallback addressCallback
    ) {
        Cluster cluster = new Cluster();
        {
            cluster.setClusterId("%s-%s".formatted(name, secure ? "secure" : "insecure"));
            cluster.setClusterName("Generated");
            cluster.setClusterType(secure ? ClusterType.hosted_insecure : ClusterType.hosted_secure);
            cluster.setAdminUrl(addressCallback.serviceAddr(1));
        }

        Cluster.BaseLine baseline = new Cluster.BaseLine();
        baseline.setVersion("v25.3.4.linux-amd64");
        baseline.setServiceAddr("localhost:9091");
        baseline.setListenAddr(":+25257");
        baseline.setAdvertiseAddr(":+25257");
        baseline.setAdvertiseProxyAddr(":+35257");
        baseline.setSqlAddr("localhost:+26257");
        baseline.setHttpAddr(":+8080");
        cluster.setBaseLine(baseline);

        Cluster.HAProxy loadBalancer = new Cluster.HAProxy();
        loadBalancer.setHttpAddr(":8070");
        loadBalancer.setRpcAddr(":26257");
        loadBalancer.setStatsAddr(":7070");
        cluster.setHaProxy(loadBalancer);

        final String firstNodeIP = addressCallback.firstNodeIP();

        {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            if (secure) {
                dataSourceProperties.setUrl(
                        "jdbc:postgresql://%s/defaultdb?sslmode=require".formatted(firstNodeIP));
                dataSourceProperties.setUsername("craig");
                dataSourceProperties.setPassword("cockroach");
            } else {
                dataSourceProperties.setUrl(
                        "jdbc:postgresql://%s/defaultdb?sslmode=disable".formatted(firstNodeIP));
                dataSourceProperties.setUsername("root");
                dataSourceProperties.setPassword("");
            }
            cluster.setDataSourceProperties(dataSourceProperties);
        }

        IntStream.rangeClosed(1, regions.size()).forEach(nodeId -> {
            Cluster.Node node = new Cluster.Node();
            node.setLocality("region=%s,zone=%s".formatted(
                    regions.get(nodeId - 1),
                    zones.get(nodeId - 1))
            );
            node.setId(nodeId);
            node.setName("n%d".formatted(nodeId));
            node.setServiceAddr(addressCallback.serviceAddr(nodeId));
            node.setAdvertiseAddr(addressCallback.advertiseAddr(nodeId));
            node.setAdvertiseProxyAddr(addressCallback.advertiseProxyAddr(nodeId));
            node.setListenAddr(addressCallback.listenAddr(nodeId));
            node.setSqlAddr(addressCallback.sqlAddr(nodeId));
            node.setHttpAddr(addressCallback.httpAddr(nodeId));

            if (secure) {
                node.setCertHosts(List.of("localhost", firstNodeIP, "localhost", "127.0.0.1"));
            }

            cluster.getNodes().add(node);
        });

        return cluster;
    }
}

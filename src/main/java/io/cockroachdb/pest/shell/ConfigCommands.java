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
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Root;
import io.cockroachdb.pest.shell.support.ClusterProvider;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class ConfigCommands extends AbstractCommand {
    private static final char[] ALPHA = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    @PostConstruct
    public void init() {
        if (StringUtils.hasLength(getApplicationProperties().getDefaultClusterId())) {
            useCluster(getApplicationProperties().getDefaultClusterId());
        } else {
            useCluster(getApplicationProperties().getClusterIds().stream().findFirst().orElseThrow());
        }
    }

    @ShellMethod(value = "Select default cluster ID to use in commands", key = {"use-cluster", "use"})
    public void useCluster(@ShellOption(help = "Cluster ID to use (must be of hosted cluster type)",
            valueProvider = ClusterProvider.class) String clusterId) {
        selectCluster(clusterId);
    }

    @ShellMethod(value = "Generate application YAML for localhost", key = {"gen-local-cfg"})
    public void generateLocalConfig(
            @ShellOption(help = "Name prefix", defaultValue = "cloud") String name,
            @ShellOption(help = "Output file path", defaultValue = "application-gen.yml") String output,
            @ShellOption(help = "Regions without number suffix (like 'eu-central,eu-west,..')", defaultValue = "eu-central")
            List<String> regions,
            @ShellOption(help = "Number of zones per region (min 1)", defaultValue = "3") int numZones,
            @ShellOption(help = "Number of nodes per zone (min 1)", defaultValue = "1") int numNodes

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

        generateConfig(name, output, tiers, zones, internalIPs, internalIPs);
    }

    @ShellMethod(value = "Generate application YAML", key = {"gen-cfg"})
    public void generateConfig(
            @ShellOption(help = "Name prefix", defaultValue = "cloud") String name,
            @ShellOption(help = "Output file path", defaultValue = "application-gen.yml") String output,
            @ShellOption(help = "Region list") List<String> regions,
            @ShellOption(help = "Zone list") List<String> zones,
            @ShellOption(help = "Internal IP list") List<String> internalIPs,
            @ShellOption(help = "External IP list") List<String> externalIPs
    ) {
        Assert.isTrue(!regions.isEmpty(), "regions is empty");
        Assert.isTrue(!zones.isEmpty(), "zones is empty");
        Assert.state(regions.size() == zones.size(), "size of regions != size of zones");
        Assert.state(internalIPs.size() == externalIPs.size(), "size of internal ips != size of external ips");
        Assert.state(regions.size() == internalIPs.size(), "size of regions != size of ips");

        ApplicationProperties applicationProperties = new ApplicationProperties();

        AddressCallback callback = new AddressCallback() {
            @Override
            public String firstNodeIP() {
                return internalIPs.get(0);
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

        Cluster insecureCluster
                = generateClusterProperties(name, regions, zones, false, callback);
        Cluster secureCluster
                = generateClusterProperties(name, regions, zones, true, callback);
        applicationProperties.setDefaultClusterId(insecureCluster.getClusterId());
        applicationProperties.getClusters().add(insecureCluster);
        applicationProperties.getClusters().add(secureCluster);

        writeApplicationProperties(applicationProperties, yaml -> {
            System.out.println();
            System.out.println(yaml);

            try {
                logger.info("Writing generated YAML to '%s'".formatted(output));
                Files.writeString(Path.of(output), yaml,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @ShellMethod(value = "Print application YAML", key = {"print-cfg"})
    public void printConfig(@ShellOption(help = "Output file path", defaultValue = ShellOption.NULL) String output) {
        writeApplicationProperties(getApplicationProperties(), yaml -> {
            System.out.println(yaml);

            if (output != null) {
                try {
                    logger.info("Writing YAML to '%s'".formatted(output));
                    Files.writeString(Path.of(output), yaml, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
    }

    private void writeApplicationProperties(ApplicationProperties applicationProperties,
                                            Consumer<String> yaml) {
        try {
            StringWriter sw = new StringWriter();
            yamlObjectMapper
                    .writerFor(Root.class)
                    .writeValue(sw, new Root(applicationProperties));
            yaml.accept(sw.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Cluster generateClusterProperties(
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
            cluster.setVersion("v25.3.3.linux-amd64");
        }

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

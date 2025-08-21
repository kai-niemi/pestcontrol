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
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.pest.model.ApplicationSettings;
import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.NodeSettings;
import io.cockroachdb.pest.model.Root;

@ShellComponent
@ShellCommandGroup(Constants.SETUP_COMMANDS)
public class ConfigCommands {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    private static final char[] ALPHA = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    @ShellMethod(value = "Generate application YAML", key = {"gen-yml"})
    public void generateYaml(
            @ShellOption(help = "Name prefix", defaultValue = "cloud") String name,
            @ShellOption(help = "Output file path", defaultValue = "test.yml") String output,
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

        createYaml(name, output, tiers, zones, internalIPs, internalIPs);
    }

    @ShellMethod(value = "Create application YAML", key = {"create-yml"})
    public void createYaml(
            @ShellOption(help = "Name prefix", defaultValue = "cloud") String name,
            @ShellOption(help = "Output file path", defaultValue = "test.yml") String output,
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

        ApplicationSettings applicationSettings = new ApplicationSettings();

        AddressCallback callback = new AddressCallback() {
            @Override
            public String firstNodeIP() {
                return internalIPs.get(0);
            }

            private boolean isLocalIP(String ip) {
                return Objects.equals(ip, "localhost") || Objects.equals(ip, "127.0.0.1");
            }

            @Override
            public String adminURL(int id) {
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

        ClusterSettings insecureCluster
                = generateClusterProperties(name, regions, zones, false, callback);
        ClusterSettings secureCluster
                = generateClusterProperties(name, regions, zones, true, callback);
        applicationSettings.setDefaultClusterId(insecureCluster.getClusterId());
        applicationSettings.getClusters().add(insecureCluster);
        applicationSettings.getClusters().add(secureCluster);

        try {
            StringWriter sw = new StringWriter();

            yamlObjectMapper
                    .writerFor(Root.class)
                    .writeValue(sw, new Root(applicationSettings));

            System.out.println();
            System.out.println(sw);

            logger.info("Writing generated YAML to '%s'".formatted(output));

            Files.writeString(Path.of(output), sw.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ClusterSettings generateClusterProperties(
            String name,
            List<String> regions,
            List<String> zones,
            boolean secure,
            AddressCallback addressCallback
    ) {

        ClusterSettings clusterSettings = new ClusterSettings();
        {
            clusterSettings.setClusterId("%s-%s".formatted(name, secure ? "secure" : "insecure"));
            clusterSettings.setClusterName("Generated");
            clusterSettings.setClusterType(secure ? ClusterType.hosted_insecure : ClusterType.hosted_secure);
            clusterSettings.setAdminUrl(addressCallback.adminURL(1));
            clusterSettings.setVersion("v25.2.2.linux-amd64");
            clusterSettings.setSecure(secure);
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
            clusterSettings.setDataSourceProperties(dataSourceProperties);
        }

        IntStream.rangeClosed(1, regions.size()).forEach(nodeId -> {
            NodeSettings nodeSettings = new NodeSettings();
            nodeSettings.setLocality("region=%s,zone=%s".formatted(
                    regions.get(nodeId - 1),
                    zones.get(nodeId - 1))
            );
            nodeSettings.setId(nodeId);
            nodeSettings.setName("n%d".formatted(nodeId));
            nodeSettings.setUrl(addressCallback.adminURL(nodeId));
            nodeSettings.setAdvertiseAddr(addressCallback.advertiseAddr(nodeId));
            nodeSettings.setAdvertiseProxyAddr(addressCallback.advertiseProxyAddr(nodeId));
            nodeSettings.setListenAddr(addressCallback.listenAddr(nodeId));
            nodeSettings.setSqlAddr(addressCallback.sqlAddr(nodeId));
            nodeSettings.setHttpAddr(addressCallback.httpAddr(nodeId));

            if (secure) {
                nodeSettings.setCertHosts(List.of("localhost", firstNodeIP, "localhost", "127.0.0.1"));
            }

            clusterSettings.getNodes().add(nodeSettings);
        });

        return clusterSettings;
    }
}

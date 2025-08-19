package io.cockroachdb.pest.shell;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.NodeProperties;
import io.cockroachdb.pest.model.Root;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class ConfigCommands {
    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    private static final char[] ZONES = "abcdefg".toCharArray();

    @ShellMethod(value = "Generate application YAML", key = {"gen"})
    public void generateYaml(
            @ShellOption(help = "Output file path") String output,
//            @ShellOption(help = "Region list") List<String> regions,
//            @ShellOption(help = "Zone list") List<String> zones,
            @ShellOption(help = "Zone list") int nodes

    ) {
//        region=aws-eu-central-1,zone=aws-eu-central-1a
//        region=aws-eu-central-1,zone=aws-eu-central-1b
//        region=aws-eu-central-1,zone=aws-eu-central-1c
        List<String> ip = new ArrayList<>();
        List<String> regions = new ArrayList<>();
        List<String> zones = new ArrayList<>();

        AtomicInteger region = new AtomicInteger(1);
        AtomicInteger zone = new AtomicInteger(1);
        IntStream.rangeClosed(1, nodes).forEach(value -> {
            ip.add("localhost");
            if (value % 3 == 0) {
                region.incrementAndGet();
                zone.set(1);
            }

            regions.add("region-" + region.get());
            regions.add("zone-" + zone.getAndIncrement() + value + ZONES[value % ZONES.length]);
        });

        generateYaml(output, regions, zones, ip, ip);
    }

    @ShellMethod(value = "Generate application YAML", key = {"gen"})
    public void generateYaml(
            @ShellOption(help = "Output file path") String output,
            @ShellOption(help = "Region list") List<String> regions,
            @ShellOption(help = "Zone list") List<String> zones,
            @ShellOption(help = "Internal IP list") List<String> internalIPs,
            @ShellOption(help = "External IP list") List<String> externalIPs
    ) {
        Assert.isTrue(!regions.isEmpty(), "regions is empty");
        Assert.isTrue(!zones.isEmpty(), "zones is empty");
        Assert.state(regions.size() == zones.size(), "size of regions != size of zones");
        Assert.state(internalIPs.size() == externalIPs.size(), "size of regions != size of zones");

        ApplicationProperties applicationProperties = new ApplicationProperties();
        applicationProperties.setDefaultClusterId("cloud-insecure");
        applicationProperties.getClusters()
                .add(generateClusterProperties(regions, zones, internalIPs, externalIPs, false));
        applicationProperties.getClusters()
                .add(generateClusterProperties(regions, zones, internalIPs, externalIPs, true));

        try {
            StringWriter sw = new StringWriter();

            yamlObjectMapper
                    .writerFor(Root.class)
                    .writeValue(sw, new Root(applicationProperties));

            System.out.println();
            System.out.println(sw);

            Files.writeString(Path.of(output), sw.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ClusterProperties generateClusterProperties(
            @ShellOption(help = "Region list") List<String> regions,
            @ShellOption(help = "Zone list") List<String> zones,
            @ShellOption(help = "Internal IP list") List<String> internalIPs,
            @ShellOption(help = "External IP list") List<String> externalIPs,
            @ShellOption(help = "Secure mode", defaultValue = "false") boolean secure
    ) {
        final String firstIP = internalIPs.stream().findFirst().orElseThrow();

        ClusterProperties clusterProperties = new ClusterProperties();
        clusterProperties.setSecure(secure);
        clusterProperties.setClusterId("cloud-%s".formatted(secure ? "secure" : "insecure"));
        clusterProperties.setClusterName("Generated");
        clusterProperties.setClusterType(secure ? ClusterType.hosted_insecure : ClusterType.hosted_secure);
        clusterProperties.setAdminUrl("http://%s:8080".formatted(firstIP));
        clusterProperties.setVersion("v25.2.2.linux-amd64");

        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        if (secure) {
            dataSourceProperties.setUrl(
                    "jdbc:postgresql://%s/defaultdb?sslmode=require".formatted(firstIP));
            dataSourceProperties.setUsername("craig");
            dataSourceProperties.setPassword("cockroach");
        } else {
            dataSourceProperties.setUrl(
                    "jdbc:postgresql://%s/defaultdb?sslmode=disable".formatted(firstIP));
            dataSourceProperties.setUsername("root");
            dataSourceProperties.setPassword("");
        }
        clusterProperties.setDataSourceProperties(dataSourceProperties);

        AtomicInteger nodeId = new AtomicInteger();
        internalIPs.forEach(ip -> {
            nodeId.incrementAndGet();

            NodeProperties nodeProperties = new NodeProperties();
            nodeProperties.setUrl("http://%s:9090".formatted(firstIP));
            nodeProperties.setName("n%d".formatted(nodeId.get()));
            nodeProperties.setId(nodeId.get());
            nodeProperties.setLocality("region=%s,zone=%s"
                    .formatted(
                            regions.get(nodeId.get() - 1),
                            zones.get(nodeId.get() - 1))
            );
            nodeProperties.setAdvertiseAddr("%s:25257".formatted(ip));
            nodeProperties.setAdvertiseProxyAddr("%s:35257".formatted(ip));
            nodeProperties.setListenAddr("%s:25257".formatted(ip));
            nodeProperties.setSqlAddr("%s:26257".formatted(ip));
            nodeProperties.setHttpAddr("%s:8080".formatted(ip));

            if (secure) {
                nodeProperties.setCertHosts(List.of("localhost", ip, "localhost", "127.0.0.1"));
            }
            clusterProperties.getNodes().add(nodeProperties);
        });

        return clusterProperties;
    }
}

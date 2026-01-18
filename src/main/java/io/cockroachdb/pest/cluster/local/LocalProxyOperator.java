package io.cockroachdb.pest.cluster.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import io.cockroachdb.pest.cluster.ProxyOperator;
import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.NetworkAddress;

public class LocalProxyOperator implements ProxyOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cluster cluster;

    private final ApplicationProperties.Directories directories;

    public LocalProxyOperator(Cluster cluster, ApplicationProperties.Directories directories) {
        this.cluster = cluster;
        this.directories = directories;
    }

    @Override
    public String genHAProxyCfg(Integer nodeId) throws IOException {
        Path templateFile = directories.getConfigDirPath()
                .resolve(cluster.isSecure() ? "haproxy-secure.cfg" : "haproxy-insecure.cfg");

        String haproxyConfig =
                "# DO NOT EDIT - file is overwritten by gen-haproxy command\n\n"
                + Files.readString(templateFile);

        haproxyConfig = new PropertyPlaceholderHelper("${", "}")
                .replacePlaceholders(haproxyConfig,
                        placeholderName -> switch (placeholderName.toLowerCase()) {
                            case "bind-stats" -> "bind %s".formatted(cluster.getHaProxy().getStatsAddr());
                            case "bind-rpc" -> "bind %s".formatted(cluster.getHaProxy().getRpcAddr());
                            case "servers-rpc" -> {
                                List<String> servers = new ArrayList<>();
                                cluster.getNodes().forEach(np -> {
                                    servers.add("server cockroach%d %s check port %s\n"
                                            .formatted(np.getId(),
                                                    np.getJoinAddress(),
                                                    NetworkAddress.from(np.getHttpAddr()).getPort().orElse(8080)
                                            ));
                                });
                                yield String.join("    ", servers);
                            }
                            case "bind-http" -> "bind %s".formatted(cluster.getHaProxy().getHttpAddr());
                            case "servers-http" -> {
                                List<String> servers = new ArrayList<>();
                                cluster.getNodes().forEach(np -> {
                                    servers.add("server cockroach%d %s check port %s\n"
                                            .formatted(np.getId(),
                                                    np.getHttpAddr(),
                                                    NetworkAddress.from(np.getHttpAddr()).getPort().orElse(8080)
                                            ));
                                });
                                yield String.join("    ", servers);
                            }
                            default -> placeholderName;
                        });

        Path configFilePath = directories.getConfigDirPath()
                .resolve("haproxy.cfg");

        Files.writeString(configFilePath, haproxyConfig,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);

        logger.info("Created '" + configFilePath + "' from " + templateFile);

        return "";
    }

    @Override
    public String startHAProxy(Integer nodeId) throws IOException {
        return CommandBuilder.builder()
                .withBaseDir(directories.getBaseDirPath())
                .withCommand("start-haproxy")
                .execute();
    }

    @Override
    public String stopHAProxy(Integer nodeId) throws IOException {
        return CommandBuilder.builder()
                .withBaseDir(directories.getBaseDirPath())
                .withCommand("stop-haproxy")
                .execute();
    }
}

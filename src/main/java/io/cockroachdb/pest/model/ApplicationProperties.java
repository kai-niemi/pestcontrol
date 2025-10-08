package io.cockroachdb.pest.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties implements InitializingBean {
    @Valid
    private Directories directories = new Directories();

    @Valid
    private Pool pool = new Pool();

    @Valid
    private Toxiproxy toxiproxy = new Toxiproxy();

    @NotEmpty
    private List<@Valid Cluster> clusters = new ArrayList<>();

    private String defaultClusterId;

    @Override
    public void afterPropertiesSet() {
        this.clusters.forEach(Cluster::validatePostCreation);

        try {
            Files.createDirectories(directories.getBinDirPath());
            Files.createDirectories(directories.getCertsDirPath());
            Files.createDirectories(directories.getDataDirPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DataSourceProperties getDataSourceProperties(String clusterId) {
        return getClusterById(clusterId).getDataSourceProperties();
    }

    public Cluster getClusterById(String clusterId) {
        return getClusterByIdAndType(clusterId, EnumSet.allOf(ClusterType.class));
    }

    public Cluster getClusterByIdAndType(String clusterId, EnumSet<ClusterType> requiredTypes) {
        Cluster cluster = getClusters()
                .stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No cluster configuration with id: " + clusterId));
        if (!requiredTypes.contains(cluster.getClusterType())) {
            throw new IllegalArgumentException("Cluster configuration is not of expected types '%s' but '%s'"
                    .formatted(requiredTypes, cluster.getClusterType()));
        }
        return cluster;
    }

    @JsonIgnore
    public List<String> getClusterIds() {
        return getClusters()
                .stream()
                .map(Cluster::getClusterId)
                .toList();
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public Toxiproxy getToxiproxy() {
        return toxiproxy;
    }

    public void setToxiproxy(Toxiproxy toxiProxy) {
        this.toxiproxy = toxiProxy;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public String getDefaultClusterId() {
        return defaultClusterId;
    }

    public void setDefaultClusterId(String defaultClusterId) {
        this.defaultClusterId = defaultClusterId;
    }

    public Directories getDirectories() {
        return directories;
    }

    public void setDirectories(@Valid Directories directories) {
        this.directories = directories;
    }

    public static class Toxiproxy {
        private String host;

        private int port;

        private boolean enabled;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    @Validated
    public static class Pool {
        @NotNull
        private Integer threadPoolMaxSize;

        @NotNull
        private Integer maxTotal;

        @NotNull
        private Integer maxConnPerRoute;

        public Integer getThreadPoolMaxSize() {
            return threadPoolMaxSize;
        }

        public void setThreadPoolMaxSize(Integer threadPoolMaxSize) {
            this.threadPoolMaxSize = threadPoolMaxSize;
        }

        public Integer getMaxConnPerRoute() {
            return maxConnPerRoute;
        }

        public void setMaxConnPerRoute(Integer maxConnPerRoute) {
            this.maxConnPerRoute = maxConnPerRoute;
        }

        public Integer getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(Integer maxTotal) {
            this.maxTotal = maxTotal;
        }
    }

    @Validated
    public static class Directories {
        @NotEmpty
        private String baseDir;

        @NotEmpty
        private String binDir;

        @NotEmpty
        private String certsDir;

        @NotEmpty
        private String dataDir;

        public String getBaseDir() {
            return baseDir;
        }

        @JsonIgnore
        public Path getBaseDirPath() {
            return Paths.get(baseDir);
        }

        public void setBaseDir(String baseDir) {
            this.baseDir = baseDir;
        }

        public String getBinDir() {
            return binDir;
        }

        @JsonIgnore
        public Path getBinDirPath() {
            return getBaseDirPath().resolve(binDir);
        }

        public void setBinDir(String binDir) {
            this.binDir = binDir;
        }

        public String getCertsDir() {
            return certsDir;
        }

        @JsonIgnore
        public Path getCertsDirPath() {
            return getBaseDirPath().resolve(certsDir);
        }

        public void setCertsDir(String certsDir) {
            this.certsDir = certsDir;
        }

        public String getDataDir() {
            return dataDir;
        }

        @JsonIgnore
        public Path getDataDirPath() {
            return getBaseDirPath().resolve(dataDir);
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }
    }
}

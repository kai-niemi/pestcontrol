package io.cockroachdb.pest.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties implements InitializingBean {
    @Valid
    @NotNull
    private Directories directories;

    @Valid
    @NotNull
    private Pool pool;

    @Valid
    private Toxiproxy toxiproxy;

    @NotEmpty
    private List<@Valid ClusterProperties> clusterProperties = new ArrayList<>();

    private String defaultClusterId;

    @Override
    public void afterPropertiesSet() {
        this.clusterProperties.forEach(ClusterProperties::afterPropertiesSet);
    }

    public DataSourceProperties getDataSourceProperties(String clusterId) {
        return getClusterPropertiesById(clusterId).getDataSourceProperties();
    }

    public ClusterProperties getClusterPropertiesById(String clusterId) {
        return getClusterPropertiesByIdAndType(clusterId, EnumSet.allOf(ClusterType.class));
    }

    public ClusterProperties getClusterPropertiesByIdAndType(String clusterId, EnumSet<ClusterType> requiredTypes) {
        ClusterProperties clusterProperties = getClusterProperties()
                .stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No cluster configuration with id: " + clusterId));
        if (!requiredTypes.contains(clusterProperties.getClusterType())) {
            throw new IllegalArgumentException("Cluster configuration is not of expected types '%s' but '%s'"
                    .formatted(requiredTypes, clusterProperties.getClusterType()));
        }
        return clusterProperties;
    }

    @JsonIgnore
    public List<String> getClusterIds() {
        return getClusterProperties()
                .stream()
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    @JsonGetter("clusters")
    public List<ClusterProperties> getClusterProperties() {
        return clusterProperties;
    }

    @JsonSetter("clusters")
    public void setClusterProperties(List<ClusterProperties> clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    public Toxiproxy getToxiproxyProperties() {
        return toxiproxy;
    }

    public void setToxiproxyProperties(Toxiproxy toxiProxy) {
        this.toxiproxy = toxiProxy;
    }

    public Pool getPoolProperties() {
        return pool;
    }

    public void setPoolProperties(Pool pool) {
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Directories {
        @NotEmpty
        private String baseDir;

        @NotEmpty
        private String binDir;

        @NotEmpty
        private String certsDir;

        @NotEmpty
        private String dataDir;

        public String getBinDir() {
            return binDir;
        }

        @JsonIgnore
        public Path getBinDirPath() {
            return Paths.get(binDir);
        }

        public void setBinDir(String binDir) {
            this.binDir = binDir;
        }

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

        public String getCertsDir() {
            return certsDir;
        }

        public String getDataDir() {
            return dataDir;
        }

        @JsonIgnore
        public Path getDataDirPath() {
            return Paths.get(dataDir);
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }

        @JsonIgnore
        public Path getCertsDirPath() {
            return Paths.get(certsDir);
        }

        public void setCertsDir(String certsDir) {
            this.certsDir = certsDir;
        }
    }
}

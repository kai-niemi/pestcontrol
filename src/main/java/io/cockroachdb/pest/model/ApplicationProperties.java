package io.cockroachdb.pest.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.rekawek.toxiproxy.ToxiproxyClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@JsonIgnoreProperties({"directories", "pool", "toxiproxy"})
public class ApplicationProperties implements InitializingBean {
    @Valid
    private Directories directories = new Directories();

    @Valid
    private Pool pool = new Pool();

    @NotEmpty
    private List<@Valid Cluster> clusters = new ArrayList<>();

    private ToxiProxy toxiProxy = new ToxiProxy();

    private String defaultClusterId;

    private boolean dryRunLocalCommands;

    @Override
    public void afterPropertiesSet() throws IOException {
        this.clusters.forEach(x -> x.postConstruct(this));

        Files.createDirectories(directories.getBinDirPath());
        Files.createDirectories(directories.getCertsDirPath());
        Files.createDirectories(directories.getDataDirPath());
    }

    public ToxiproxyClient createToxiProxyClient() {
        return new ToxiproxyClient(toxiProxy.getHost(), toxiProxy.getPort());
    }

    public Cluster getClusterById(String clusterId) {
        return getClusters()
                .stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No cluster configuration with id: " + clusterId));
    }

    @JsonIgnore
    public List<String> getClusterIds() {
        return getClusters()
                .stream()
                .map(Cluster::getClusterId)
                .toList();
    }

    public boolean isDryRunLocalCommands() {
        return dryRunLocalCommands;
    }

    public void setDryRunLocalCommands(boolean dryRunLocalCommands) {
        this.dryRunLocalCommands = dryRunLocalCommands;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public ToxiProxy getToxiProxy() {
        return toxiProxy;
    }

    public void setToxiProxy(ToxiProxy toxiProxy) {
        this.toxiProxy = toxiProxy;
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

        @NotEmpty
        private String configDir;

        public String getBaseDir() {
            return baseDir;
        }

        public void setBaseDir(String baseDir) {
            this.baseDir = baseDir;
        }

        @JsonIgnore
        public Path getBaseDirPath() {
            return Paths.get(baseDir);
        }

        public String getBinDir() {
            return binDir;
        }

        public void setBinDir(String binDir) {
            this.binDir = binDir;
        }

        @JsonIgnore
        public Path getBinDirPath() {
            return getBaseDirPath().resolve(binDir);
        }

        public String getCertsDir() {
            return certsDir;
        }

        public void setCertsDir(String certsDir) {
            this.certsDir = certsDir;
        }

        @JsonIgnore
        public Path getCertsDirPath() {
            return getBaseDirPath().resolve(certsDir);
        }

        public String getDataDir() {
            return dataDir;
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }

        @JsonIgnore
        public Path getDataDirPath() {
            return getBaseDirPath().resolve(dataDir);
        }

        public String getConfigDir() {
            return configDir;
        }

        public void setConfigDir(String configDir) {
            this.configDir = configDir;
        }

        @JsonIgnore
        public Path getConfigDirPath() {
            return getBaseDirPath().resolve(configDir);
        }
    }

    @Validated
    public static class ToxiProxy {
        @NotNull
        private String host;

        @NotNull
        private Integer port;

        private boolean enabled;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

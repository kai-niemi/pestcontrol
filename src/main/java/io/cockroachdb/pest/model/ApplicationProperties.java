package io.cockroachdb.pest.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.cluster.ClusterOperator;

@Component
@Validated
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    @NotEmpty
    private List<@Valid ClusterProperties> clusters = new ArrayList<>();

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    @Valid
    private ProxyProperties toxiproxy;

    @Valid
    @NotNull
    private ClientProperties http;

    private Integer threadPoolMaxSize;

    private Integer samplePeriodSeconds;

    private String baseDir;

    private String certsDir;

    private String defaultClusterId;

    @PostConstruct
    public void init() {
        clusters.forEach(ClusterProperties::init);
    }

    public ClusterOperator clusterOperator(String clusterId) {
        return clusterOperator(getClusterPropertiesById(clusterId).getClusterType());
    }

    public ClusterOperator clusterOperator(ClusterType clusterType) {
        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No operator found for cluster type: " + clusterType));
    }

    public ClosableDataSource getDataSource(String clusterId) {
        return dataSourceFactory.apply(getClusterPropertiesById(clusterId).getDataSourceProperties());
    }

    public List<String> getClusterIds() {
        return getClusters()
                .stream()
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    public List<String> getClusterIds(EnumSet<ClusterType> requiredTypes) {
        return getClusters()
                .stream()
                .filter(x -> requiredTypes.contains(x.getClusterType()))
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    public ClusterProperties getClusterPropertiesById(String clusterId) {
        return getClusterPropertiesById(clusterId, EnumSet.allOf(ClusterType.class));
    }

    public ClusterProperties getClusterPropertiesById(String clusterId, EnumSet<ClusterType> requiredTypes) {
        ClusterProperties clusterProperties = getClusters()
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

    public List<ClusterProperties> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterProperties> clusters) {
        this.clusters = clusters;
    }

    public ProxyProperties getToxiproxy() {
        return toxiproxy;
    }

    public void setToxiproxy(ProxyProperties toxiproxy) {
        this.toxiproxy = toxiproxy;
    }

    public Integer getSamplePeriodSeconds() {
        return samplePeriodSeconds;
    }

    public void setSamplePeriodSeconds(Integer samplePeriodSeconds) {
        this.samplePeriodSeconds = samplePeriodSeconds;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public Path getScriptDirectory() {
        return Paths.get(baseDir);
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getCertsDir() {
        return certsDir;
    }

    public Path getCertsDirectory() {
        return Paths.get(certsDir);
    }

    public void setCertsDir(String certsDir) {
        this.certsDir = certsDir;
    }

    public Integer getThreadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    public void setThreadPoolMaxSize(Integer threadPoolMaxSize) {
        this.threadPoolMaxSize = threadPoolMaxSize;
    }

    public ClientProperties getHttp() {
        return http;
    }

    public void setHttp(ClientProperties http) {
        this.http = http;
    }

    public String getDefaultClusterId() {
        return defaultClusterId;
    }

    public void setDefaultClusterId(String defaultClusterId) {
        this.defaultClusterId = defaultClusterId;
    }
}

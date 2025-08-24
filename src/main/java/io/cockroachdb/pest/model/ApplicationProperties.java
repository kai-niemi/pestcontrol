package io.cockroachdb.pest.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.config.ClosableDataSource;

@Component
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties implements InitializingBean {
    @NotEmpty
    private List<@Valid ClusterProperties> clusters = new ArrayList<>();

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    @Valid
    private ToxiProxyProperties toxiProxyProperties;

    @Valid
    @NotNull
    private HttpProperties http;

    private Integer threadPoolMaxSize;

    private Integer samplePeriodSeconds;

    private String baseDir;

    private String certsDir;

    private String defaultClusterId;

    @Override
    public void afterPropertiesSet() {
        this.clusters.forEach(ClusterProperties::afterPropertiesSet);
    }

    public ClosableDataSource getDataSource(String clusterId) {
        return dataSourceFactory.apply(getClusterPropertiesById(clusterId).getDataSourceProperties());
    }

    public ClusterOperator getClusterOperatorById(String clusterId) {
        return getClusterOperatorByType(getClusterPropertiesById(clusterId).getClusterType());
    }

    public ClusterOperator getClusterOperatorByType(ClusterType clusterType) {
        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No operator found for cluster type: " + clusterType));
    }

    public ClusterProperties getClusterPropertiesById(String clusterId) {
        return getClusterPropertiesByIdAndType(clusterId, EnumSet.allOf(ClusterType.class));
    }

    public ClusterProperties getClusterPropertiesByIdAndType(String clusterId, EnumSet<ClusterType> requiredTypes) {
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

    @JsonIgnore
    public List<String> getClusterIds() {
        return getClusters()
                .stream()
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    public List<ClusterProperties> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterProperties> clusters) {
        this.clusters = clusters;
    }

    public ToxiProxyProperties getToxiProxy() {
        return toxiProxyProperties;
    }

    public void setToxiProxy(ToxiProxyProperties toxiProxyProperties) {
        this.toxiProxyProperties = toxiProxyProperties;
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

    @JsonIgnore
    public Path getCertsDirPath() {
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

    public HttpProperties getHttp() {
        return http;
    }

    public void setHttp(HttpProperties http) {
        this.http = http;
    }

    public String getDefaultClusterId() {
        return defaultClusterId;
    }

    public void setDefaultClusterId(String defaultClusterId) {
        this.defaultClusterId = defaultClusterId;
    }
}

package io.cockroachdb.pestcontrol.model;

import java.util.ArrayList;
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

import io.cockroachdb.pestcontrol.config.ClosableDataSource;
import io.cockroachdb.pestcontrol.operator.ClusterOperator;

@Component
@ConfigurationProperties(prefix = "application")
@Validated
public class ApplicationProperties {
    @Valid
    private List<ClusterProperties> clusters = new ArrayList<>();

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    @Valid
    private ToxiproxyProperties toxiproxy;

    @PostConstruct
    public void init() {
        clusters.forEach(ClusterProperties::init);
    }

    public ClusterOperator clusterOperator(String clusterId)
            throws UnsupportedOperationException {
        ClusterType clusterType = getClusterPropertiesById(clusterId).getClusterType();

        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No operator found for cluster type " + clusterType));
    }

    public ClosableDataSource getDataSource(String clusterId) {
        return dataSourceFactory.apply(getClusterPropertiesById(clusterId).getDataSourceProperties());
    }

    public ClusterProperties getClusterPropertiesById(String clusterId) {
        return getClusters()
                .stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No cluster configuration with id: " + clusterId));
    }

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

    public ToxiproxyProperties getToxiproxy() {
        return toxiproxy;
    }

    public void setToxiproxy(ToxiproxyProperties toxiproxy) {
        this.toxiproxy = toxiproxy;
    }

}

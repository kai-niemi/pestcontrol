package io.cockroachdb.pest.cluster.cloud;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.local.CommandBuilder;
import io.cockroachdb.pest.cluster.local.MetaDataRepository;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.status.ClusterStatus;
import io.cockroachdb.pest.model.status.NodeStatus;

public class CloudStatusOperator implements StatusOperator {
    private final Cluster cluster;

    private final RestClient restClient;

    private final MetaDataRepository metaDataRepository;

    private final String authToken;

    public CloudStatusOperator(Cluster cluster,
                               RestClient restClient,
                               MetaDataRepository metaDataRepository,
                               ApplicationProperties applicationProperties) {
        this.cluster = cluster;
        this.restClient = restClient;
        this.metaDataRepository = metaDataRepository;

        try {
            // todo
            this.authToken = CommandBuilder.builder()
                    .withBaseDir(applicationProperties.getDirectories().getBaseDirPath())
                    .withCommand("login")
                    .withFlags("--user-name=%s".formatted(cluster.getDataSourceProperties().getUrl()))
                    .withFlags("--url=%s".formatted(cluster.getLoginUrl()))
                    .execute();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String clusterVersion() {
        return metaDataRepository.queryClusterVersion(cluster);
    }

    @Override
    public List<NodeStatus> nodeStatus() {
        return restClient
                .get()
                .uri(cluster.getAdminUrl() + "/_status/nodes")
                .header("Cookie", authToken)
                .retrieve()
                .toEntity(ClusterStatus.class)
                .getBody()
                .getNodes();
    }

    @Override
    public NodeStatus nodeStatusById(Integer id) {
        return restClient
                .get()
                .uri(cluster.getAdminUrl() + "/_status/nodes/" + id)
                .header("Cookie", authToken)
                .retrieve()
                .toEntity(NodeStatus.class)
                .getBody();
    }
}

package io.cockroachdb.pest.cluster.cloud;

import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.local.MetaDataRepository;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.status.ClusterStatus;
import io.cockroachdb.pest.model.status.NodeStatus;

public class CloudStatusOperator implements StatusOperator {
    private final Cluster cluster;

    private final RestClient restClient;

    private final MetaDataRepository metaDataRepository;

    public CloudStatusOperator(Cluster cluster,
                               RestClient restClient,
                               MetaDataRepository metaDataRepository) {
        this.cluster = cluster;
        this.restClient = restClient;
        this.metaDataRepository = metaDataRepository;
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
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cookie", Objects.requireNonNull(cluster.getAuthToken(),
                "Cloud API authentication token not specified"));

        return restClient
                .post()
                .uri(cluster.getAdminUrl() + "/_status/nodes")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(ClusterStatus.class)
                .getBody()
                .getNodes();
    }

    @Override
    public NodeStatus nodeStatusById(Integer id) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("cookie", Objects.requireNonNull(cluster.getAuthToken(),
                "Cloud API authentication token not specified"));

        return restClient
                .post()
                .uri(cluster.getAdminUrl() + "/_status/nodes/" + id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(NodeStatus.class)
                .getBody();
    }
}

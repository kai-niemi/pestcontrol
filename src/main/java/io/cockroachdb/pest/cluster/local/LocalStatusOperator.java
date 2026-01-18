package io.cockroachdb.pest.cluster.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.ResourceNotFoundException;
import io.cockroachdb.pest.cluster.repository.MetaDataRepository;
import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeDetails;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterTypes;

public class LocalStatusOperator implements StatusOperator {
    private final RestClient restClient;

    private final MetaDataRepository metaDataRepository;

    private final Cluster cluster;

    private transient String sessionToken;

    public LocalStatusOperator(Cluster cluster,
                               RestClient restClient,
                               MetaDataRepository metaDataRepository) {
        this.cluster = cluster;
        this.restClient = restClient;
        this.metaDataRepository = metaDataRepository;

        if (ClusterTypes.isSecure(cluster.getClusterType())) {
            this.sessionToken = login();
        } else {
            this.sessionToken = "";
        }
    }

    private String login() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", cluster.getDataSourceProperties().getUsername());
        map.add("password", cluster.getDataSourceProperties().getPassword());

        ResponseEntity<String> responseEntity = restClient
                .post()
                .uri(cluster.getAdminUrl() + "/api/v2/login/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(String.class);

        return JsonPath.parse(responseEntity.getBody()).read("$.session", String.class);
    }

    private void logout() {
        ResponseEntity<String> responseEntity = restClient
                .post()
                .uri(cluster.getAdminUrl() + "/api/v2/logout/")
                .header("X-Cockroach-API-Session", sessionToken)
                .retrieve()
                .toEntity(String.class);

        JsonPath.parse(responseEntity.getBody()).read("$.logged_out", Boolean.class);
    }

    private List<NodeDetail> queryNodeDetails() {
        Assert.notNull(sessionToken, "sessionToken is null");

        // There's no way to narrow this down other than by pagination
        ResponseEntity<NodeDetails> responseEntity = restClient
                .get()
                .uri(cluster.getAdminUrl() + "/api/v2/nodes/")
                .header("X-Cockroach-API-Session", sessionToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(NodeDetails.class);

        return Objects.requireNonNull(responseEntity.getBody()).getNodes();
    }

    @Override
    public void close() {
        sessionToken = null;
        logout();
    }

    @Override
    public String queryClusterVersion() {
        return metaDataRepository.queryClusterVersion(cluster);
    }

    @Override
    public NodeDetail queryNodeDetailById(Integer id) {
        return queryNodeDetails()
                .stream()
                .filter(nodeStatus -> nodeStatus.getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeStatus queryNodeStatusById(Integer id) {
        return metaDataRepository.queryNodeStatusById(cluster, id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeModel queryNodeById(Integer id) {
        return queryAllNodes()
                .stream()
                .filter(node -> node.getNodeDetail().getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public List<NodeModel> queryAllNodes() {
        List<NodeModel> nodeModels = new ArrayList<>();

        List<NodeStatus> nodeStatusList = metaDataRepository.queryNodeStatus(cluster);

        queryNodeDetails().forEach(nodeDetail -> nodeStatusList.stream()
                .filter(nodeStatus -> nodeStatus.getId().equals(nodeDetail.getNodeId()))
                .findFirst()
                .ifPresentOrElse(nodeStatus -> {
                    nodeModels.add(new NodeModel(cluster.getClusterId(), nodeDetail, nodeStatus));
                }, () -> {
                    nodeModels.add(new NodeModel(cluster.getClusterId(), nodeDetail, new NodeStatus()));
                }));

        return nodeModels;
    }
}

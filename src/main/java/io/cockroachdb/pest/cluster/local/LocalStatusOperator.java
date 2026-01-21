package io.cockroachdb.pest.cluster.local;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.model.status.ClusterStatus;
import io.cockroachdb.pest.model.status.NodeStatus;

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
        if (sessionToken != null) {
            ResponseEntity<String> responseEntity = restClient
                    .post()
                    .uri(cluster.getAdminUrl() + "/api/v2/logout/")
                    .header("X-Cockroach-API-Session", sessionToken)
                    .retrieve()
                    .toEntity(String.class);
            JsonPath.parse(responseEntity.getBody()).read("$.logged_out", Boolean.class);
            sessionToken = null;
        }
    }

    @Override
    public void close() {
        logout();
    }

    @Override
    public String clusterVersion() {
        return metaDataRepository.queryClusterVersion(cluster);
    }

    @Override
    public List<NodeStatus> nodeStatus() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", cluster.getDataSourceProperties().getUsername());
        map.add("password", cluster.getDataSourceProperties().getPassword());

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
        map.add("username", cluster.getDataSourceProperties().getUsername());
        map.add("password", cluster.getDataSourceProperties().getPassword());

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

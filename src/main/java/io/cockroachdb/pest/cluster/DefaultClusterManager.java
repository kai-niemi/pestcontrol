package io.cockroachdb.pest.cluster;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeDetails;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;

@Component
public class DefaultClusterManager implements ClusterManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> sessionTokens = new ConcurrentHashMap<>();

    private final Map<String, List<NodeModel>> fallbackModels = new HashMap<>();

    private CredentialsHandler credentialsHandler = new CredentialsHandler() {
    };

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private RestClientProvider restClientProvider;

    @Autowired
    private ClusterQuery clusterQuery;

    @Override
    public void setCredentialsHandler(CredentialsHandler credentialsHandler) {
        this.credentialsHandler = credentialsHandler;
    }

    @Override
    public List<String> getClusterIds() {
        return applicationProperties.getClusters()
                .stream()
                .map(Cluster::getClusterId)
                .toList();
    }

    @Override
    public String getClusterVersion(String clusterId) {
        return clusterQuery.queryClusterVersion(getCluster(clusterId));
    }

    @Override
    public String login(String clusterId, String userName, String password) {
        Cluster cluster = getCluster(clusterId);

        if (EnumSet.of(ClusterType.hosted_insecure).contains(cluster.getClusterType())) {
            logger.info("Implicit login for cluster type: %s".formatted(cluster.getClusterType()));
            sessionTokens.put(clusterId, "");
            return "";
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", userName);
        map.add("password", password);

        ResponseEntity<String> responseEntity = restClientProvider.apply(cluster.getClusterType())
                .post()
                .uri(cluster.getAdminUrl() + "/api/v2/login/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(String.class);

        String token = JsonPath.parse(responseEntity.getBody()).read("$.session", String.class);
        logger.info("Login token: " + token);

        sessionTokens.put(clusterId, token);

        return token;
    }

    @Override
    public boolean logout(String clusterId) {
        Cluster cluster = getCluster(clusterId);

        String sessionToken = findSessionToken(clusterId);

        ResponseEntity<String> responseEntity = restClientProvider.apply(cluster.getClusterType())
                .post()
                .uri(cluster.getAdminUrl() + "/api/v2/logout/")
                .header("X-Cockroach-API-Session", sessionToken)
                .retrieve()
                .toEntity(String.class);

        boolean outcome = JsonPath.parse(responseEntity.getBody()).read("$.logged_out", Boolean.class);
        if (outcome) {
            sessionTokens.remove(clusterId);
            logger.info("Logout command successful");
        } else {
            logger.warn("Logout command failed: " + responseEntity.getBody());
        }

        return outcome;
    }

    public List<NodeDetail> queryNodeDetails(Cluster cluster, String sessionToken) {
        Assert.notNull(sessionToken, "sessionToken is null");

        // There's no way to narrow this down other than by pagination
        ResponseEntity<NodeDetails> responseEntity = restClientProvider.apply(cluster.getClusterType())
                .get()
                .uri(cluster.getAdminUrl() + "/api/v2/nodes/")
                .header("X-Cockroach-API-Session", sessionToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(NodeDetails.class);

        return Objects.requireNonNull(responseEntity.getBody()).getNodes();
    }

    private String findSessionToken(String clusterId) {
        if (!sessionTokens.containsKey(clusterId)) {
            Pair<String, String> credentials = credentialsHandler
                    .getAuthenticationCredentials(clusterId);
            return login(clusterId, credentials.getFirst(), credentials.getSecond());
        }
        return sessionTokens.get(clusterId);
    }

    public Optional<NodeDetail> queryNodeDetailById(Cluster cluster, String sessionToken,
                                                    Integer nodeId) {
        return queryNodeDetails(cluster, sessionToken)
                .stream()
                .filter(nodeStatus -> nodeStatus.getNodeId().equals(nodeId))
                .findFirst();
    }

    @Override
    public boolean hasSessionToken(String clusterId) {
        return sessionTokens.containsKey(clusterId);
    }

    @Override
    public NodeDetail queryNodeDetailById(String clusterId, Integer id) {
        return queryNodeDetailById(getCluster(clusterId), findSessionToken(clusterId), id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeStatus queryNodeStatusById(String clusterId, Integer id) {
        return clusterQuery.queryNodeStatusById(getCluster(clusterId), id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeModel queryNodeById(String clusterId, Integer id) {
        return queryAllNodes(clusterId)
                .stream()
                .filter(node -> node.getNodeDetail().getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public List<NodeModel> queryAllNodes(String clusterId) {
        final List<NodeModel> nodeModelList = new ArrayList<>();

        try {
            Cluster cluster = getCluster(clusterId);
            List<NodeStatus> nodeStatusList = clusterQuery.queryNodeStatus(cluster);
            List<NodeDetail> nodeDetailList = queryNodeDetails(cluster, findSessionToken(clusterId));

            nodeDetailList.forEach(nodeDetail -> nodeStatusList.stream()
                    .filter(nodeStatus -> nodeStatus.getId().equals(nodeDetail.getNodeId()))
                    .findFirst()
                    .ifPresentOrElse(nodeStatus -> {
                        nodeModelList.add(new NodeModel(clusterId, nodeDetail, nodeStatus));
                    }, () -> {
                        nodeModelList.add(new NodeModel(clusterId, nodeDetail, new NodeStatus()));
                    }));

            fallbackModels.put(clusterId, nodeModelList);
        } catch (Exception e) {
            if (fallbackModels.containsKey(clusterId)) {
                logger.warn("Error querying cluster status - using fallback", e);
                nodeModelList.addAll(fallbackModels.get(clusterId));
                nodeModelList.forEach(nodeModel -> {
                    nodeModel.getNodeStatus().setIsLive("false");
                });
            } else {
                throw e;
            }
        }
        return nodeModelList;
    }

    @Override
    public Cluster getCluster(String clusterId) {
        return applicationProperties.getClusterById(clusterId);
    }
}

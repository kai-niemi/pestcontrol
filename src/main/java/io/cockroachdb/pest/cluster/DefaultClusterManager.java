package io.cockroachdb.pest.cluster;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pest.api.cluster.NodeModel;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.schema.NodeDetail;
import io.cockroachdb.pest.schema.NodeStatus;

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

    private RestClient restClient(ClusterProperties clusterProperties) {
        return restClientProvider.matches(clusterProperties);
    }

    @Override
    public void setCredentialsHandler(CredentialsHandler credentialsHandler) {
        this.credentialsHandler = credentialsHandler;
    }

    @Override
    public List<String> getClusterIds() {
        return applicationProperties.getClusters()
                .stream()
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    @Override
    public String getClusterVersion(String clusterId) {
        return clusterQuery.queryClusterVersion(getClusterProperties(clusterId));
    }

    @Override
    public String login(String clusterId, String userName, String password) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        if (EnumSet.of(ClusterType.remote_insecure, ClusterType.hosted_insecure)
                .contains(clusterProperties.getClusterType())) {
            logger.info("Implicit login for cluster type: %s"
                    .formatted(clusterProperties.getClusterType()));
            sessionTokens.put(clusterId, "");
            return "";
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", userName);
        map.add("password", password);

        ResponseEntity<String> responseEntity = restClient(clusterProperties)
                .post()
                .uri(clusterProperties.getAdminUrl() + "/api/v2/login/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(String.class);

        String token = JsonPath.parse(responseEntity.getBody()).read("$.session", String.class);

        sessionTokens.put(clusterId, token);

        logger.debug("Login successful - received session token: " + token);

        return token;
    }

    @Override
    public boolean logout(String clusterId) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        String sessionToken = findSessionToken(clusterId);

        ResponseEntity<String> responseEntity = restClient(clusterProperties)
                .post()
                .uri(clusterProperties.getAdminUrl() + "/api/v2/logout/")
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

    private String findSessionToken(String clusterId) {
        if (!sessionTokens.containsKey(clusterId)) {
            Pair<String, String> credentials = credentialsHandler
                    .getAuthenticationCredentials(clusterId);
            return login(clusterId, credentials.getFirst(), credentials.getSecond());
        }
        return sessionTokens.get(clusterId);
    }

    @Override
    public boolean hasSessionToken(String clusterId) {
        return sessionTokens.containsKey(clusterId);
    }

    @Override
    public NodeDetail queryNodeDetailById(String clusterId, Integer id) {
        return clusterQuery.queryNodeDetailById(getClusterProperties(clusterId), findSessionToken(clusterId),  id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeStatus queryNodeStatusById(String clusterId, Integer id) {
        return clusterQuery.queryNodeStatusById(getClusterProperties(clusterId), id)
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
        List<NodeModel> nodeModelList = new ArrayList<>();

        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        try {
            List<NodeStatus> nodeStatusList = clusterQuery.queryNodeStatus(clusterProperties);
            List<NodeDetail> nodeDetailList = clusterQuery.queryNodeDetails(clusterProperties,
                    findSessionToken(clusterId));

            nodeDetailList.forEach(nodeDetail -> nodeStatusList.stream()
                    .filter(nodeStatus -> nodeStatus.getId().equals(nodeDetail.getNodeId()))
                    .findFirst()
                    .ifPresentOrElse(nodeStatus -> {
                        nodeModelList.add(new NodeModel(clusterId, nodeDetail, nodeStatus));
                    }, () -> {
                        nodeModelList.add(new NodeModel(clusterId, nodeDetail, new NodeStatus()));
                    }));

            fallbackModels.put(clusterId, nodeModelList);
            return nodeModelList;
        } catch (Exception e) {
            logger.warn("Error querying cluster status", e);

            if (fallbackModels.containsKey(clusterId)) {
                List<NodeModel> cachedList = fallbackModels.get(clusterId);
                cachedList.forEach(nodeModel -> {
                    nodeModel.getNodeStatus().setIsLive("false");
                });
                return cachedList;
            } else {
                if (ClusterTypes.isHosted((clusterProperties.getClusterType()))) {
                    clusterProperties.getNodes()
                            .forEach(nodeProperties -> {
                                nodeModelList.add(new NodeModel(clusterId,
                                        NodeDetail.from(nodeProperties),
                                        NodeStatus.from(nodeProperties)));
                            });
                }

                throw e;
            }
        }

    }

    @Override
    public ClusterProperties getClusterProperties(String clusterId) {
        return applicationProperties.getClusterPropertiesById(clusterId);
    }

    @Override
    public ClusterOperator getClusterOperator(String clusterId)
            throws UnsupportedOperationException {
        return applicationProperties.clusterOperator(clusterId);
    }

    @Override
    public ClusterOperator getClusterOperator(ClusterType clusterType)
            throws UnsupportedOperationException {
        return applicationProperties.clusterOperator(clusterType);
    }
}

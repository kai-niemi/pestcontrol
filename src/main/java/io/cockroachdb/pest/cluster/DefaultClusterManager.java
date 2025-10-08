package io.cockroachdb.pest.cluster;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeStatus;

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

    @Autowired
    private ObjectProvider<ClusterOperator> clusterOperators;

    private RestClient restClient(Cluster cluster) {
        return restClientProvider.matches(cluster);
    }

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
        return clusterQuery.queryClusterVersion(getClusterProperties(clusterId));
    }

    @Override
    public String login(String clusterId, String userName, String password) {
        Cluster cluster = getClusterProperties(clusterId);

        if (EnumSet.of(ClusterType.hosted_insecure)
                .contains(cluster.getClusterType())) {
            logger.info("Implicit login for cluster type: %s"
                    .formatted(cluster.getClusterType()));
            sessionTokens.put(clusterId, "");
            return "";
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", userName);
        map.add("password", password);

        ResponseEntity<String> responseEntity = restClient(cluster)
                .post()
                .uri(cluster.getAdminUrl() + "/api/v2/login/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(String.class);

        String token = JsonPath.parse(responseEntity.getBody())
                .read("$.session", String.class);

        sessionTokens.put(clusterId, token);

        logger.debug("Login successful - received session token: " + token);

        return token;
    }

    @Override
    public boolean logout(String clusterId) {
        Cluster cluster = getClusterProperties(clusterId);

        String sessionToken = findSessionToken(clusterId);

        ResponseEntity<String> responseEntity = restClient(cluster)
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
        return clusterQuery.queryNodeDetailById(getClusterProperties(clusterId), findSessionToken(clusterId), id)
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
        final List<NodeModel> nodeModelList = new ArrayList<>();

        try {
            Cluster cluster = getClusterProperties(clusterId);
            List<NodeStatus> nodeStatusList = clusterQuery.queryNodeStatus(cluster);
            List<NodeDetail> nodeDetailList = clusterQuery.queryNodeDetails(cluster,
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
    public Cluster getClusterProperties(String clusterId) {
        return applicationProperties.getClusterByIdAndType(clusterId, EnumSet.allOf(ClusterType.class));
    }

    @Override
    public Cluster getClusterProperties(String clusterId, EnumSet<ClusterType> clusterTypes) {
        return applicationProperties.getClusterByIdAndType(clusterId, clusterTypes);
    }

    @Override
    public ClusterOperator getClusterOperator(String clusterId) {
        return getClusterOperatorByType(getClusterProperties(clusterId).getClusterType());
    }

    public ClusterOperator getClusterOperatorByType(ClusterType clusterType) {
        return clusterOperators
                .stream()
                .filter(x -> x.supports(clusterType))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No operator found for cluster type: " + clusterType));
    }
}

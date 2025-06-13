package io.cockroachdb.pest.cluster;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pest.api.cluster.NodeModel;
import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.repository.ClusterRepository;
import io.cockroachdb.pest.repository.JdbcClusterRepository;
import io.cockroachdb.pest.schema.NodeDetail;
import io.cockroachdb.pest.schema.NodeDetails;
import io.cockroachdb.pest.schema.NodeStatus;

@Component
public class DefaultClusterManager implements ClusterManager {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> sessionTokens = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @Autowired
    private RestClientProvider restClientProvider;

    // Initial failing credentials handler
    private CredentialsHandler credentialsHandler = new CredentialsHandler() {
    };

    @Override
    public void setCredentialsHandler(CredentialsHandler credentialsHandler) {
        this.credentialsHandler = credentialsHandler;
    }

    protected String findSessionToken(String clusterId) {
        if (!sessionTokens.containsKey(clusterId)) {
            Pair<String, String> credentials = credentialsHandler
                    .getAuthenticationCredentials(clusterId);
            return login(clusterId, credentials.getFirst(), credentials.getSecond());
        }
        return sessionTokens.get(clusterId);
    }

    private List<NodeStatus> queryNodeStatus(ClusterProperties clusterProperties) {
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(clusterProperties.getDataSourceProperties())) {

            ClusterRepository clusterRepository = new JdbcClusterRepository(dataSource);
            String json = clusterRepository.queryNodeStatus();

            return Objects.isNull(json) ? List.of() :
                    objectMapper.readerForListOf(NodeStatus.class).readValue(json);
        } catch (DataAccessException e) {
            throw new ClientErrorException("Unable to query node status", e);
        } catch (JsonProcessingException e) {
            throw new ClientErrorException("Unable to read status query JSON", e);
        }
    }

    private Optional<NodeStatus> queryNodeStatusById(ClusterProperties clusterProperties, Integer nodeId) {
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(clusterProperties.getDataSourceProperties())) {

            ClusterRepository clusterRepository = new JdbcClusterRepository(dataSource);
            String json = clusterRepository.queryNodeStatusById(nodeId);

            NodeStatus nodeStatus = Objects.isNull(json) ? null
                    : objectMapper.readerFor(NodeStatus.class)
                    .readValue(json);
            return Optional.ofNullable(nodeStatus);
        } catch (DataAccessException e) {
            throw new ClientErrorException("Unable to query node #" + nodeId + " status", e);
        } catch (JsonProcessingException e) {
            throw new ClientErrorException("Unable to read status query JSON", e);
        }
    }

    private RestClient restClient(ClusterProperties clusterProperties) {
        return restClientProvider.matches(clusterProperties);
    }

    private List<NodeDetail> queryNodeDetails(ClusterProperties clusterProperties) {
        String sessionToken = findSessionToken(clusterProperties.getClusterId());

        // There's no way to narrow this down other than by pagination
        ResponseEntity<NodeDetails> responseEntity = restClient(clusterProperties)
                .get()
                .uri(clusterProperties.getAdminUrl() + "/api/v2/nodes/")
                .header("X-Cockroach-API-Session", sessionToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(NodeDetails.class);

        return Objects.requireNonNull(responseEntity.getBody()).getNodes();
    }

    private Optional<NodeDetail> queryNodeDetailById(ClusterProperties clusterProperties, Integer nodeId) {
        return queryNodeDetails(clusterProperties)
                .stream()
                .filter(nodeStatus -> nodeStatus.getNodeId().equals(nodeId))
                .findFirst();
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
        ClusterProperties clusterProperties = getClusterProperties(clusterId);
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(clusterProperties.getDataSourceProperties())) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForObject("select version()", String.class);
        } catch (DataAccessException e) {
            throw new ServerErrorException("Unable to query cluster version", e);
        }
    }

    @Override
    public String login(String clusterId, String userName, String password) {
        ClusterProperties clusterProperties = getClusterProperties(clusterId);

        if (EnumSet.of(ClusterType.local_insecure, ClusterType.remote_insecure,
                        ClusterType.hosted_insecure, ClusterType.hosted_secure)
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

    @Override
    public boolean hasSessionToken(String clusterId) {
        return sessionTokens.containsKey(clusterId);
    }

    @Override
    public NodeDetail queryNodeDetailById(String clusterId, Integer id) {
        return queryNodeDetailById(getClusterProperties(clusterId), id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeStatus queryNodeStatusById(String clusterId, Integer id) {
        return queryNodeStatusById(getClusterProperties(clusterId), id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    // Cache of models to use if cluster becomes unavailable

    private final Map<String, List<NodeModel>> fallbackModels = new HashMap<>();

    @Override
    public List<NodeModel> queryAllNodes(String clusterId) {
        try {
            List<NodeModel> nodeModelList = new ArrayList<>();
            ClusterProperties clusterProperties = getClusterProperties(clusterId);
            List<NodeStatus> nodeStatusList = queryNodeStatus(clusterProperties);
            List<NodeDetail> nodeDetailList = queryNodeDetails(clusterProperties);

            nodeDetailList.forEach(nodeDetail -> nodeStatusList.stream()
                    .filter(nodeStatus -> nodeStatus.getId().equals(nodeDetail.getNodeId()))
                    .findFirst()
                    .ifPresentOrElse(nodeStatus -> {
                        nodeModelList.add(new NodeModel(clusterId, nodeDetail, nodeStatus));
                    }, () -> {
                        nodeModelList.add(new NodeModel(clusterId, nodeDetail, new NodeStatus()));
                        logger.warn("Unable to pair node detail (id: %s) with node status"
                                .formatted(nodeDetail.getNodeId()));
                    }));

            fallbackModels.put(clusterId, nodeModelList);
            return nodeModelList;
        } catch (Exception e) {
            if (fallbackModels.containsKey(clusterId)) {
                List<NodeModel> cachedList = fallbackModels.get(clusterId);
                cachedList.forEach(nodeModel -> {
                    nodeModel.getNodeStatus().setIsLive("false");
//                    nodeModel.getNodeStatus().setIsAvailable("false");
                });
                return cachedList;
            } else {
                throw e;
            }
        }

    }

//    public List<NodeModel> getNodes(List<Tier> tiers) {
//        return nodes
//                .getContent()
//                .stream()
//                .filter(node -> node.getLocality().matches(tiers))
//                .sorted(Comparator.comparing(NodeModel::getId))
//                .toList();
//                        .sorted((n1, n2) -> n1.getLocality().toTiers()
//                .compareToIgnoreCase(n2.getLocality().toTiers()))
//    }

    @Override
    public NodeModel queryNodeById(String clusterId, Integer id) {
        return queryAllNodes(clusterId)
                .stream()
                .filter(node -> node.getNodeDetail().getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
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
}

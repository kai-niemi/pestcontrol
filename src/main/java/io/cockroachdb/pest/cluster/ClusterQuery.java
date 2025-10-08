package io.cockroachdb.pest.cluster;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.repository.ClusterRepository;
import io.cockroachdb.pest.repository.JdbcClusterRepository;
import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeDetails;
import io.cockroachdb.pest.cluster.model.NodeStatus;

@Component
public class ClusterQuery {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestClientProvider restClientProvider;

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    public String queryClusterVersion(Cluster cluster) {
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(cluster.getDataSourceProperties())) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForObject("select version()", String.class);
        } catch (DataAccessException e) {
            throw new ServerErrorException("Unable to query cluster version", e);
        }
    }

    public Optional<NodeDetail> queryNodeDetailById(Cluster cluster, String sessionToken,
                                                    Integer nodeId) {
        return queryNodeDetails(cluster, sessionToken)
                .stream()
                .filter(nodeStatus -> nodeStatus.getNodeId().equals(nodeId))
                .findFirst();
    }

    public List<NodeDetail> queryNodeDetails(Cluster cluster, String sessionToken) {
        Assert.notNull(sessionToken, "sessionToken is null");
        // There's no way to narrow this down other than by pagination
        ResponseEntity<NodeDetails> responseEntity = restClientProvider.matches(cluster)
                .get()
                .uri(cluster.getAdminUrl() + "/api/v2/nodes/")
                .header("X-Cockroach-API-Session", sessionToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(NodeDetails.class);
        return Objects.requireNonNull(responseEntity.getBody()).getNodes();
    }

    public List<NodeStatus> queryNodeStatus(Cluster cluster) {
        try (ClosableDataSource dataSource = dataSourceFactory.apply(cluster
                .getDataSourceProperties())) {
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

    public Optional<NodeStatus> queryNodeStatusById(Cluster cluster, Integer nodeId) {
        try ( ClosableDataSource dataSource = dataSourceFactory.apply(cluster.getDataSourceProperties())) {
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
}

package io.cockroachdb.pest.cluster;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.pest.cluster.model.NodeStatus;
import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.repository.ClusterRepository;
import io.cockroachdb.pest.repository.JdbcClusterRepository;

@Component
public class ClusterQuery {
    @Autowired
    private ObjectMapper objectMapper;

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
        try (ClosableDataSource dataSource = dataSourceFactory.apply(cluster.getDataSourceProperties())) {
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

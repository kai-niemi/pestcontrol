package io.cockroachdb.pest.cluster;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.cockroachdb.pest.AbstractIntegrationTest;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.api.cluster.NodeModel;
import io.cockroachdb.pest.schema.NodeDetail;
import io.cockroachdb.pest.schema.NodeStatus;
import io.cockroachdb.pest.util.CalendarVersion;

public class ClusterManagerTest extends AbstractIntegrationTest {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    private Optional<String> sessionToken = Optional.empty();

    private void assertSessionToken() {
        ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(getTagValue());

        String token = this.sessionToken.orElseGet(() -> clusterManager
                .login(clusterProperties.getClusterId(),
                        clusterProperties.getDataSourceProperties().getUsername(),
                        clusterProperties.getDataSourceProperties().getPassword()
                )
        );

        Assertions.assertNotNull(token);
        this.sessionToken = Optional.of(token);
    }

    @BeforeAll
    public void setupTestOnce() {
        Assertions.assertTrue(clusterManager
                        .getClusterIds().contains(getTagValue()),
                "Cluster id not found: "
                + getTagValue() + " of "
                + String.join(",", clusterManager.getClusterIds()));
    }

    @Order(0)
    @Test
    public void whenGetClusterVersion_thenPrintActualCalendarVersions() {
        String version = clusterManager.getClusterVersion(getTagValue());
        Assertions.assertNotNull(version);

        logger.info("Cluster version: %s".formatted(version));

        CalendarVersion v = CalendarVersion.of(version);
        Assertions.assertNotNull(v);

        logger.info("Parsed version: %s".formatted(v));
    }

    @Order(1)
    @Test
    public void whenLogin_thenExpectSessionToken() {
        assertSessionToken();
    }

    @Order(2)
    @Test
    public void whenQueryNodeStatusById_thenExpectSuccess() {
        assertSessionToken();

        NodeStatus nodeStatus = clusterManager.queryNodeStatusById(getTagValue(), 1);
        Assertions.assertNotNull(nodeStatus);
        Assertions.assertEquals(1, nodeStatus.getId());

        logger.info("Received node status: " + nodeStatus);
    }

    @Order(3)
    @Test
    public void whenQueryNodeDetailById_thenExpectSuccess() {
        assertSessionToken();

        NodeDetail nodeDetail = clusterManager.queryNodeDetailById(getTagValue(), 1);
        Assertions.assertNotNull(nodeDetail);
        Assertions.assertEquals(1, nodeDetail.getNodeId());

        logger.info("Received node detail: " + nodeDetail);
    }

    @Order(4)
    @Test
    public void whenQueryNodeModelById_thenExpectSuccess() {
        assertSessionToken();

        NodeModel nodeModel = clusterManager.queryNodeById(getTagValue(), 1);
        Assertions.assertNotNull(nodeModel);
        Assertions.assertEquals(1, nodeModel.getNodeId());

        logger.info("Received node model: " + nodeModel);
    }

    @Order(5)
    @Test
    public void whenQueryAllNodeModels_thenExpectSuccess() {
        assertSessionToken();

        List<NodeModel> nodeModels = clusterManager.queryAllNodes(getTagValue());
        Assertions.assertNotNull(nodeModels);
        nodeModels.forEach(nodeModel -> {
            logger.info("Received node model: " + nodeModel);
        });
    }

    @Order(6)
    @Test
    public void whenDisruptNode_thenExpectSuccess() {
        assertSessionToken();
//        clusterManager.disruptNode(getTagValue(), 1);
    }

    @Order(7)
    @Test
    public void whenRecoverNode_thenExpectSuccess() {
        assertSessionToken();
//        clusterManager.recoverNode(getTagValue(), 1);
    }

    @Order(10)
    @Test
    public void whenLogout_thenExpectSuccess() {
        boolean result = clusterManager.logout(getTagValue());
        Assertions.assertTrue(result);
    }
}

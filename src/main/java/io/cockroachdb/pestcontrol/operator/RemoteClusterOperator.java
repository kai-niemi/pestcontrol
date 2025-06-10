package io.cockroachdb.pestcontrol.operator;

import java.util.EnumSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.Hop;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.shell.client.HypermediaClient;
import static io.cockroachdb.pestcontrol.api.LinkRelations.AGENT_FORM_REL;
import static io.cockroachdb.pestcontrol.api.LinkRelations.AGENT_START_REL;
import static io.cockroachdb.pestcontrol.api.LinkRelations.CLUSTER_COLL_REL;
import static io.cockroachdb.pestcontrol.api.LinkRelations.CLUSTER_REL;
import static io.cockroachdb.pestcontrol.api.LinkRelations.CURIE_NAMESPACE;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@Component
public class RemoteClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                .contains(clusterType);
    }

    @Override
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        Link rootLink = clusterProperties.findNodePropertiesById(nodeId).getBaseUrl();

        Link startLink = hypermediaClient.from(rootLink)
                .follow(curied(CURIE_NAMESPACE, CLUSTER_COLL_REL).value())
                .follow(Hop.rel(curied(CURIE_NAMESPACE, CLUSTER_REL).value())
                        .withParameter("clusterId", clusterProperties.getClusterId()))
                .follow(Hop.rel(curied(CURIE_NAMESPACE, AGENT_FORM_REL).value())
                        .withParameter("clusterId", clusterProperties.getClusterId()))
                .follow(curied(CURIE_NAMESPACE, AGENT_START_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", clusterProperties.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(startLink, clusterProperties, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public void stopNode(ClusterProperties clusterProperties, Integer nodeId) {
    }

    @Override
    public void init(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void killNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disruptNodes(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recoverNodes(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }
}

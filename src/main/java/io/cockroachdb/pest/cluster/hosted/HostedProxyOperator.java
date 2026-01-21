package io.cockroachdb.pest.cluster.hosted;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.Hop;
import org.springframework.http.ResponseEntity;

import io.cockroachdb.pest.cluster.ProxyOperator;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.LinkRelations;
import io.cockroachdb.pest.util.HypermediaClient;
import static io.cockroachdb.pest.model.LinkRelations.CLUSTERS_REL;
import static io.cockroachdb.pest.model.LinkRelations.CURIE_NAMESPACE;
import static io.cockroachdb.pest.model.LinkRelations.NODE_GEN_HAPROXY_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_START_HAPROXY_REL;
import static io.cockroachdb.pest.model.LinkRelations.NODE_STOP_HAPROXY_REL;
import static io.cockroachdb.pest.model.LinkRelations.OPERATOR_REL;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

public class HostedProxyOperator implements ProxyOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HypermediaClient hypermediaClient;

    private final Cluster cluster;

    public HostedProxyOperator(Cluster cluster, HypermediaClient hypermediaClient) {
        this.cluster = cluster;
        this.hypermediaClient = hypermediaClient;
    }

    private Link operatorLink(int nodeId) {
        return hypermediaClient.from(cluster.getNodeById(nodeId).getServiceLink())
                .follow(curied(CURIE_NAMESPACE, CLUSTERS_REL).value())
                .follow(Hop.rel(curied(CURIE_NAMESPACE, LinkRelations.OPERATOR_TEMPLATE_REL).value())
                        .withParameter("clusterType", cluster.getClusterType()))
                .follow(curied(CURIE_NAMESPACE, OPERATOR_REL).value())
                .asTemplatedLink();
    }

    @Override
    public String genHAProxyCfg(Integer nodeId) {
        Link operatorLink = operatorLink(nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_GEN_HAPROXY_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String startHAProxy(Integer nodeId) {
        Link operatorLink = operatorLink(nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_START_HAPROXY_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }

    @Override
    public String stopHAProxy(Integer nodeId) {
        Link operatorLink = operatorLink(nodeId);

        Link actionLink = hypermediaClient.from(operatorLink)
                .follow(curied(CURIE_NAMESPACE, NODE_STOP_HAPROXY_REL).value())
                .asTemplatedLink()
                .expand(Map.of(
                        "clusterId", cluster.getClusterId(),
                        "nodeId", nodeId));

        ResponseEntity<String> response = hypermediaClient.post(actionLink, cluster, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        return response.getBody();
    }
}

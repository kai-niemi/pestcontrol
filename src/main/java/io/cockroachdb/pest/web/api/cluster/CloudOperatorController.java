package io.cockroachdb.pest.web.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.web.api.MessageModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/cloud")
public class CloudOperatorController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @GetMapping
    public HttpEntity<MessageModel> index() {
        return ResponseEntity.ok(MessageModel.from("Cloud cluster operator")
                .add(linkTo(methodOn(getClass())
                        .index())
                        .withSelfRel())

                .add(linkTo(methodOn(CloudOperatorController.class)
                        .disruptLocality(null, null))
                        .withRel(LinkRelations.LOCALITY_DISRUPT_REL)
                        .withTitle("Apply locality disruption"))
                .add(linkTo(methodOn(CloudOperatorController.class)
                        .recoverLocality(null, null))
                        .withRel(LinkRelations.LOCALITY_RECOVER_REL)
                        .withTitle("Recover locality disruption"))
                .add(linkTo(methodOn(CloudOperatorController.class)
                        .disruptNode(null, null))
                        .withRel(LinkRelations.DISRUPT_NODE_REL)
                        .withTitle("Apply node disruption"))
                .add(linkTo(methodOn(CloudOperatorController.class)
                        .recoverNode(null, null))
                        .withRel(LinkRelations.RECOVER_NODE_REL)
                        .withTitle("Recover node disruption"))
        );
    }

    @PostMapping("/{clusterId}/locality/{tiers}/disrupt")
    public ResponseEntity<Void> disruptLocality(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("tiers") String tiers) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterType());
        clusterOperator.disruptLocality(cluster, tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/locality/{tiers}/recover")
    public ResponseEntity<Void> recoverLocality(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("tiers") String tiers) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterType());
        clusterOperator.recoverLocality(cluster, tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{nodeId}/disrupt")
    public ResponseEntity<Void> disruptNode(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("nodeId") Integer id) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterType());
        clusterOperator.disruptNode(cluster, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{nodeId}/recover")
    public ResponseEntity<Void> recoverNode(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("nodeId") Integer id) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterType());
        clusterOperator.recoverNode(cluster, id);
        return ResponseEntity.ok().build();
    }
}

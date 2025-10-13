package io.cockroachdb.pest.web.api.cluster;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperators;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.web.LinkRelations;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/cloud")
public class CloudController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ClusterOperators clusterOperators;

    @GetMapping("/{clusterId}")
    public ResponseEntity<EntityModel<Cluster>> index(
            @PathVariable("clusterId") String clusterId) {
        Cluster cluster = clusterManager.getCluster(clusterId);

        EntityModel<Cluster> resource = EntityModel.of(cluster);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CloudController.class)
                        .index(clusterId))
                .withSelfRel());

        if (EnumSet.of(ClusterType.cloud_dedicated,
                        ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(cluster.getClusterType())) {
            resource.add(linkTo(methodOn(CloudController.class)
                    .disruptLocality(clusterId, null))
                    .withRel(LinkRelations.LOCALITY_DISRUPT_REL)
                    .withTitle("Apply locality disruption"));
            resource.add(linkTo(methodOn(CloudController.class)
                    .recoverLocality(clusterId, null))
                    .withRel(LinkRelations.LOCALITY_RECOVER_REL)
                    .withTitle("Recover locality disruption"));
            resource.add(linkTo(methodOn(CloudController.class)
                    .disruptNode(clusterId, null))
                    .withRel(LinkRelations.DISRUPT_NODE_REL)
                    .withTitle("Apply node disruption"));
            resource.add(linkTo(methodOn(CloudController.class)
                    .recoverNode(clusterId, null))
                    .withRel(LinkRelations.RECOVER_NODE_REL)
                    .withTitle("Recover node disruption"));
        }

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{clusterId}/locality/{tiers}/disrupt")
    public ResponseEntity<Void> disruptLocality(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("tiers") String tiers) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperators.getClusterOperator(cluster.getClusterType());
        clusterOperator.disruptLocality(cluster, tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/locality/{tiers}/recover")
    public ResponseEntity<Void> recoverLocality(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("tiers") String tiers) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperators.getClusterOperator(cluster.getClusterType());
        clusterOperator.recoverLocality(cluster, tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{nodeId}/disrupt")
    public ResponseEntity<Void> disruptNode(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("nodeId") Integer id) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperators.getClusterOperator(cluster.getClusterType());
        clusterOperator.disruptNode(cluster, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{nodeId}/recover")
    public ResponseEntity<Void> recoverNode(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("nodeId") Integer id) {
        Cluster cluster = clusterManager.getCluster(clusterId);
        ClusterOperator clusterOperator = clusterOperators.getClusterOperator(cluster.getClusterType());
        clusterOperator.recoverNode(cluster, id);
        return ResponseEntity.ok().build();
    }
}

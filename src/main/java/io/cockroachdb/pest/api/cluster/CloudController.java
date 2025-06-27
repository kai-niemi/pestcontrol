package io.cockroachdb.pest.api.cluster;

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

import io.cockroachdb.pest.api.LinkRelations;
import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/cloud")
public class CloudController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/{clusterId}")
    public ResponseEntity<EntityModel<ClusterProperties>> index(
            @PathVariable("clusterId") String clusterId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);

        EntityModel<ClusterProperties> resource = EntityModel.of(clusterProperties);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(CloudController.class)
                        .index(clusterId))
                .withSelfRel());

        if (EnumSet.of(ClusterType.cloud_dedicated,
                        ClusterType.cloud_serverless,
                        ClusterType.cloud_standard)
                .contains(clusterProperties.getClusterType())) {
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

//    @PostMapping("/{clusterId}/node/{nodeId}/start")
//    public ResponseEntity<Void> startNode(@PathVariable("clusterId") String clusterId,
//                                          @PathVariable("nodeId") Integer id) {
//        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
//        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
//        clusterOperator.startNode(clusterProperties, id);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{clusterId}/node/{nodeId}/stop")
//    public ResponseEntity<Void> stopNode(@PathVariable("clusterId") String clusterId,
//                                         @PathVariable("nodeId") Integer id) {
//        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
//        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
//        clusterOperator.stopNode(clusterProperties, id);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{clusterId}/node/{nodeId}/kill")
//    public ResponseEntity<Void> killNode(@PathVariable("clusterId") String clusterId,
//                                         @PathVariable("nodeId") Integer id) {
//        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
//        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
//        clusterOperator.killNode(clusterProperties, id);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{clusterId}/node/{nodeId}/install")
//    public ResponseEntity<Void> installNode(@PathVariable("clusterId") String clusterId,
//                                            @PathVariable("nodeId") Integer id) {
//        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
//        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
//        clusterOperator.install(clusterProperties, id);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{clusterId}/node/{nodeId}/init")
//    public ResponseEntity<Void> initNode(@PathVariable("clusterId") String clusterId,
//                                         @PathVariable("nodeId") Integer id) {
//        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
//        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
//        clusterOperator.init(clusterProperties, id);
//        return ResponseEntity.ok().build();
//    }
//

    @PostMapping("/{clusterId}/locality/{tiers}/disrupt")
    public ResponseEntity<Void> disruptLocality(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("tiers") String tiers) {
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.disruptNodes(clusterManager.getClusterProperties(clusterId), tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/locality/{tiers}/recover")
    public ResponseEntity<Void> recoverLocality(@PathVariable("clusterId") String clusterId,
                                                @PathVariable("tiers") String tiers) {
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.recoverNodes(clusterManager.getClusterProperties(clusterId), tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{nodeId}/disrupt")
    public ResponseEntity<Void> disruptNode(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("nodeId") Integer id) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.disruptNode(clusterProperties, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/node/{nodeId}/recover")
    public ResponseEntity<Void> recoverNode(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("nodeId") Integer id) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.recoverNode(clusterProperties, id);
        return ResponseEntity.ok().build();
    }
}

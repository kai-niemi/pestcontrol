package io.cockroachdb.pest.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.api.LinkRelations;
import io.cockroachdb.pest.api.MessageModel;
import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterTypes;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel resource = MessageModel.from("Configured cluster properties");
        resource.add(linkTo(methodOn(ClusterController.class)
                .index())
                .withSelfRel());

        applicationProperties.getClusterIds()
                .forEach(clusterId -> resource.add(linkTo(methodOn(ClusterController.class)
                        .getCluster(clusterId))
                        .withRel(LinkRelations.CLUSTER_REL)));

        resource.add(linkTo(methodOn(ClusterController.class)
                .getCluster(null))
                .withRel(LinkRelations.CLUSTER_REL));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<EntityModel<ClusterProperties>> getCluster(@PathVariable("clusterId") String clusterId) {
        ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(clusterId);

        EntityModel<ClusterProperties> model = EntityModel.of(clusterProperties);
        model.add(linkTo(methodOn(getClass())
                .getCluster(clusterId))
                .withSelfRel());
        model.add(linkTo(methodOn(ClusterController.class)
                .getVersion(clusterId))
                .withRel(LinkRelations.VERSION_REL));
        model.add(linkTo(methodOn(NodeController.class)
                .index(clusterId))
                .withRel(LinkRelations.NODES_REL));
        model.add(linkTo(methodOn(WorkloadController.class)
                .index(clusterId))
                .withRel(LinkRelations.WORKLOADS_REL));

        if (ClusterTypes.isHosted(clusterProperties.getClusterType())) {
            model.add(linkTo(methodOn(LocalController.class)
                    .index())
                    .withRel(LinkRelations.CLUSTER_OPERATOR_REL));
        } else if (ClusterTypes.isCloud(clusterProperties.getClusterType())) {
            model.add(linkTo(methodOn(CloudController.class)
                    .index(clusterId))
                    .withRel(LinkRelations.CLUSTER_OPERATOR_REL));
        }

        return ResponseEntity.ok(model);
    }

    @GetMapping("/{clusterId}/version")
    public ResponseEntity<MessageModel> getVersion(@PathVariable("clusterId") String id) {
        return ResponseEntity.ok(MessageModel.from(clusterManager.getClusterVersion(id))
                .add(linkTo(methodOn(getClass())
                        .getVersion(id))
                        .withSelfRel()));
    }
}

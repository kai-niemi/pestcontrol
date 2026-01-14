package io.cockroachdb.pest.web.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.web.api.MessageModel;
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
                .getCluster(null)) // templated
                .withRel(LinkRelations.CLUSTER_TEMPLATE_REL));

        resource.add(linkTo(methodOn(ClusterController.class)
                .getClusterOperator(null)) // templated
                .withRel(LinkRelations.OPERATOR_TEMPLATE_REL));

        resource.add(linkTo(methodOn(CertificateController.class)
                .uploadCerts(null))
                .withRel(LinkRelations.CERTS_REL));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<EntityModel<Cluster>> getCluster(@PathVariable("clusterId") String clusterId) {
        Cluster cluster = applicationProperties.getClusterById(clusterId);

        EntityModel<Cluster> model = EntityModel.of(cluster);
        model.add(linkTo(methodOn(getClass())
                .getCluster(clusterId))
                .withSelfRel());
        model.add(linkTo(methodOn(getClass())
                .getVersion(clusterId))
                .withRel(LinkRelations.VERSION_REL));
        model.add(linkTo(methodOn(NodeController.class)
                .index(clusterId))
                .withRel(LinkRelations.NODES_REL));

        if (ClusterTypes.isHosted(cluster.getClusterType())) {
            model.add(linkTo(methodOn(LocalOperatorController.class)
                    .index())
                    .withRel(LinkRelations.OPERATOR_REL));
        } else if (ClusterTypes.isCloud(cluster.getClusterType())) {
            model.add(linkTo(methodOn(CloudOperatorController.class)
                    .index())
                    .withRel(LinkRelations.OPERATOR_REL));
        }

        return ResponseEntity.ok(model);
    }

    @GetMapping("/{clusterType}/operator")
    public ResponseEntity<MessageModel> getClusterOperator(@PathVariable("clusterType") ClusterType clusterType) {
        MessageModel model = MessageModel.from("Cluster type operator");
        model.add(linkTo(methodOn(getClass())
                .getClusterOperator(clusterType))
                .withSelfRel());

        if (ClusterTypes.isHosted(clusterType) || ClusterTypes.isLocal(clusterType)) {
            model.add(linkTo(methodOn(LocalOperatorController.class)
                    .index())
                    .withRel(LinkRelations.OPERATOR_REL));
        } else if (ClusterTypes.isCloud(clusterType)) {
            model.add(linkTo(methodOn(CloudOperatorController.class)
                    .index())
                    .withRel(LinkRelations.OPERATOR_REL));
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

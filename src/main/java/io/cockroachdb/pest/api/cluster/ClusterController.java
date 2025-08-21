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
import io.cockroachdb.pest.model.ApplicationSettings;
import io.cockroachdb.pest.model.ClusterSettings;
import io.cockroachdb.pest.model.ClusterTypes;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationSettings applicationSettings;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel resource = MessageModel.from("Configured cluster properties");
        resource.add(linkTo(methodOn(ClusterController.class)
                .index())
                .withSelfRel());

        applicationSettings.getClusterIds()
                .forEach(clusterId -> resource.add(linkTo(methodOn(ClusterController.class)
                        .getCluster(clusterId))
                        .withRel(LinkRelations.CLUSTER_REL)));

        resource.add(linkTo(methodOn(ClusterController.class)
                .getCluster(null)) // templated
                .withRel(LinkRelations.CLUSTER_TEMPLATE_REL));

        resource.add(linkTo(methodOn(CertificateController.class)
                .uploadCerts(null))
                .withRel(LinkRelations.CERTS_REL));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<EntityModel<ClusterSettings>> getCluster(@PathVariable("clusterId") String clusterId) {
        ClusterSettings clusterSettings = applicationSettings.getClusterPropertiesById(clusterId);

        EntityModel<ClusterSettings> model = EntityModel.of(clusterSettings);
        model.add(linkTo(methodOn(getClass())
                .getCluster(clusterId))
                .withSelfRel());
        model.add(linkTo(methodOn(getClass())
                .getVersion(clusterId))
                .withRel(LinkRelations.VERSION_REL));
        model.add(linkTo(methodOn(NodeController.class)
                .index(clusterId))
                .withRel(LinkRelations.NODES_REL));
        model.add(linkTo(methodOn(WorkloadController.class)
                .index(clusterId))
                .withRel(LinkRelations.WORKLOADS_REL));

        if (ClusterTypes.isHosted(clusterSettings.getClusterType())) {
            model.add(linkTo(methodOn(LocalController.class)
                    .index())
                    .withRel(LinkRelations.OPERATOR_REL));
        } else if (ClusterTypes.isCloud(clusterSettings.getClusterType())) {
            model.add(linkTo(methodOn(CloudController.class)
                    .index(clusterId))
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

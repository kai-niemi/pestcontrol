package io.cockroachdb.pest.web.api.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.model.LinkRelations;
import io.cockroachdb.pest.model.status.NodeStatus;
import io.cockroachdb.pest.web.model.ClusterModel;
import io.cockroachdb.pest.web.model.MessageModel;
import io.cockroachdb.pest.web.model.NodeStatusModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping
    public ResponseEntity<MessageModel> index() throws IOException {
        MessageModel resource = MessageModel.from("Configured cluster properties");

        resource.add(linkTo(methodOn(ClusterController.class)
                .index())
                .withSelfRel());

        for (String clusterId : applicationProperties.getClusterIds()) {
            resource.add(linkTo(methodOn(ClusterController.class)
                    .getCluster(clusterId))
                    .withRel(LinkRelations.CLUSTER_REL));
        }

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
    public ResponseEntity<ClusterModel> getCluster(
            @PathVariable String clusterId) throws IOException {
        final Cluster cluster = applicationProperties.getClusterById(clusterId);
        return ResponseEntity.ok(new ClusterModelAssembler().toModel(cluster));
    }

    @GetMapping("/{clusterType}/operator")
    public ResponseEntity<MessageModel> getClusterOperator(
            @PathVariable ClusterType clusterType) throws IOException {
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
    public ResponseEntity<MessageModel> getVersion(
            @PathVariable String clusterId) throws IOException {
        Cluster cluster = applicationProperties.getClusterById(clusterId);
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(clusterId);
        try (StatusOperator clusterStatus = clusterOperator.statusOperator(cluster)) {
            return ResponseEntity.ok(MessageModel.from(clusterStatus.clusterVersion())
                    .add(linkTo(methodOn(getClass())
                            .getVersion(clusterId))
                            .withSelfRel()));
        }
    }

    @GetMapping("/{clusterId}/status")
    public ResponseEntity<CollectionModel<NodeStatusModel>> getNodeStatus(
            @PathVariable("clusterId") String clusterId) throws IOException {
        final Cluster cluster = clusterOperatorProvider.clusterById(clusterId);

        try (StatusOperator statusOperator = clusterOperatorProvider
                .clusterOperator(clusterId)
                .statusOperator(cluster)) {

            List<NodeStatusModel> nodeModels = new ArrayList<>();
            statusOperator.nodeStatus().forEach(nodeStatus -> {
                nodeModels.add(new NodeStatusModelAssembler(cluster).toModel(nodeStatus));
            });

            return ResponseEntity.ok(CollectionModel.of(nodeModels)
                    .add(linkTo(methodOn(getClass())
                            .getNodeStatus(clusterId))
                            .withSelfRel()));
        } catch (IOException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/{clusterId}/status/{id}")
    public ResponseEntity<NodeStatusModel> getNodeStatusById(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        final Cluster cluster = clusterOperatorProvider.clusterById(clusterId);

        try (StatusOperator statusOperator = clusterOperatorProvider
                .clusterOperator(clusterId)
                .statusOperator(clusterOperatorProvider.clusterById(clusterId))) {
            NodeStatus nodeStatus = statusOperator.nodeStatusById(id);
            return ResponseEntity.ok(new NodeStatusModelAssembler(cluster).toModel(nodeStatus));
        } catch (IOException e) {
            logger.warn("", e);
            return ResponseEntity.noContent().build();
        }
    }
}

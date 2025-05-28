package io.cockroachdb.pestcontrol.api.cluster.status;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.manager.ClusterManager;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.Locality;
import io.cockroachdb.pestcontrol.operator.ClusterOperator;
import io.cockroachdb.pestcontrol.schema.NodeDetail;
import io.cockroachdb.pestcontrol.schema.NodeStatus;
import io.cockroachdb.pestcontrol.api.MessageModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster/status")
public class StatusController {
    private static Collection<Locality> nodeLocalities(List<NodeModel> models) {
        Set<Locality> localities = new LinkedHashSet<>(); // keep insertion order
        models.stream()
                .map(NodeModel::getLocality)
                .forEach(localities::add);
        return localities;
    }

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping("/{clusterId}")
    public ResponseEntity<StatusModel> getCluster(@PathVariable("clusterId") String clusterId) {
        final ClusterProperties clusterProperties
                = applicationProperties.getClusterPropertiesById(clusterId);

        final List<NodeModel> nodes
                = clusterManager.queryAllNodes(clusterId);

        StatusModel model = StatusModel.fromId(clusterId);
        model.setNodes(new NodeModelAssembler(clusterProperties.getClusterType())
                .toCollectionModel(nodes));
        model.setLocalities(new LocalityModelAssembler(clusterId, clusterProperties.getClusterType())
                .toCollectionModel(nodeLocalities(nodes)));

        return ResponseEntity.ok(new StatusModelAssembler().toModel(model));
    }

    @GetMapping("/{clusterId}/version")
    public ResponseEntity<MessageModel> getVersion(@PathVariable("clusterId") String id) {
        return ResponseEntity.ok(MessageModel.from(clusterManager.getClusterVersion(id))
                .add(linkTo(methodOn(getClass())
                        .getVersion(id))
                        .withSelfRel()));
    }

//    @GetMapping("/{clusterId}/locality")
//    public ResponseEntity<CollectionModel<LocalityModel>> getLocalities(
//            @PathVariable("clusterId") String clusterId) {
//        NodeModel nodeModel = clusterManager.queryNodeById(clusterId, 1);
//        ClusterModel clusterModel = clusterManager.getCluster(id).getBody();
//        return ResponseEntity.ok(nodeModel.getLocality().getLocalities());
//    }

    @GetMapping("/{clusterId}/locality/{tiers}")
    public ResponseEntity<LocalityModel> getLocality(@PathVariable("clusterId") String clusterId,
                                                     @PathVariable("tiers") String tiers) {
        Locality locality = Locality.fromTiers(tiers);
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        ClusterType clusterType = clusterProperties.getClusterType();
        return ResponseEntity.ok(new LocalityModelAssembler(clusterId, clusterType).toModel(locality));
    }

    @GetMapping("/{clusterId}/node")
    public ResponseEntity<CollectionModel<NodeModel>> getNodes(
            @PathVariable("clusterId") String clusterId) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        NodeModelAssembler assembler = new NodeModelAssembler(clusterProperties.getClusterType());
        return ResponseEntity.ok(assembler.toCollectionModel(
                clusterManager.queryAllNodes(clusterId)));
    }

    @GetMapping("/{clusterId}/node/{id}")
    public ResponseEntity<NodeModel> getNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        ClusterProperties clusterProperties = clusterManager.getClusterProperties(clusterId);
        NodeModelAssembler assembler = new NodeModelAssembler(clusterProperties.getClusterType());
        NodeModel nodeModel = clusterManager.queryNodeById(clusterId, id);
        return ResponseEntity.ok(assembler.toModel(nodeModel));
    }

    @GetMapping("/{clusterId}/node/{id}/detail")
    public ResponseEntity<EntityModel<NodeDetail>> getNodeDetail(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeDetail nodeDetail = clusterManager.queryNodeDetailById(clusterId, id);
        return ResponseEntity.ok(EntityModel.of(nodeDetail)
                .add(linkTo(methodOn(getClass())
                        .getNodeDetail(clusterId, id))
                        .withSelfRel()));
    }

    @GetMapping("/{clusterId}/node/{id}/status")
    public ResponseEntity<EntityModel<NodeStatus>> getNodeStatus(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeStatus nodeStatus = clusterManager.queryNodeStatusById(clusterId, id);
        return ResponseEntity.ok(EntityModel.of(nodeStatus)
                .add(linkTo(methodOn(getClass())
                        .getNodeStatus(clusterId, id))
                        .withSelfRel()));
    }

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

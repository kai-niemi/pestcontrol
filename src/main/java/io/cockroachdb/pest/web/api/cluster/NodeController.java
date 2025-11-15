package io.cockroachdb.pest.web.api.cluster;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class NodeController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/{clusterId}/node")
    public ResponseEntity<CollectionModel<NodeModel>> index(
            @PathVariable("clusterId") String clusterId) {
        final List<NodeModel> nodes = clusterManager.queryAllNodes(clusterId);
        return ResponseEntity.ok(CollectionModel.of(
                        new NodeModelAssembler().toCollectionModel(nodes))
                .add(linkTo(methodOn(NodeController.class)
                        .index(clusterId))
                        .withSelfRel()));
    }

    @GetMapping("/{clusterId}/node/{id}")
    public ResponseEntity<NodeModel> getNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeModelAssembler assembler = new NodeModelAssembler();
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
}

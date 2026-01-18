package io.cockroachdb.pest.web.api.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.cluster.ResourceNotFoundException;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.model.NodeDetail;
import io.cockroachdb.pest.cluster.model.NodeModel;
import io.cockroachdb.pest.cluster.model.NodeStatus;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class NodeController {
    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @GetMapping("/{clusterId}/node")
    public ResponseEntity<CollectionModel<NodeModel>> index(
            @PathVariable("clusterId") String clusterId) {
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(clusterId);

        try (StatusOperator clusterStatus = clusterOperator
                .statusOperator(clusterOperatorProvider.clusterById(clusterId))) {
            List<NodeModel> nodes = clusterStatus.listAllNodes();
            return ResponseEntity.ok(CollectionModel.of(new NodeModelAssembler().toCollectionModel(nodes))
                    .add(linkTo(methodOn(NodeController.class)
                            .index(clusterId))
                            .withSelfRel()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @GetMapping("/{clusterId}/node/{id}")
    public ResponseEntity<NodeModel> getNode(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        NodeModelAssembler assembler = new NodeModelAssembler();

        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(clusterId);
        try (StatusOperator clusterStatus = clusterOperator
                .statusOperator(clusterOperatorProvider.clusterById(clusterId))) {

            NodeModel nodeModel = clusterStatus.listAllNodes()
                    .stream()
                    .filter(node -> node.getNodeDetail().getNodeId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));

            return ResponseEntity.ok(assembler.toModel(nodeModel));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @GetMapping("/{clusterId}/node/{id}/detail")
    public ResponseEntity<EntityModel<NodeDetail>> getNodeDetail(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(clusterId);

        try (StatusOperator clusterStatus = clusterOperator.statusOperator(
                clusterOperatorProvider.clusterById(clusterId))) {
            NodeDetail nodeDetail = clusterStatus.nodeDetailById(id);
            return ResponseEntity.ok(EntityModel.of(nodeDetail)
                    .add(linkTo(methodOn(getClass())
                            .getNodeDetail(clusterId, id))
                            .withSelfRel()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @GetMapping("/{clusterId}/node/{id}/status")
    public ResponseEntity<EntityModel<NodeStatus>> getNodeStatus(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(clusterId);

        try (StatusOperator clusterStatus = clusterOperator.statusOperator(
                clusterOperatorProvider.clusterById(clusterId))) {
            NodeStatus nodeStatus = clusterStatus.nodeStatusById(id);
            return ResponseEntity.ok(EntityModel.of(nodeStatus)
                    .add(linkTo(methodOn(getClass())
                            .getNodeStatus(clusterId, id))
                            .withSelfRel()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

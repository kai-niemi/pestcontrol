package io.cockroachdb.pestcontrol.api.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.manager.ClusterManager;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.Locality;
import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    private static Collection<Locality> nodeLocalities(List<NodeModel> models) {
        Set<Locality> localities = new LinkedHashSet<>(); // keep insertion order
        models.stream()
                .map(NodeModel::getLocality)
                .forEach(localities::add);
        return localities;
    }

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ClusterManager clusterManager;

    @GetMapping
    public ResponseEntity<CollectionModel<ClusterModel>> index() {
        final List<ClusterModel> clusterModels = new ArrayList<>();

        applicationProperties.getClusterIds().forEach(clusterId ->
                clusterModels.add(
                        ClusterModel.from(applicationProperties.getClusterPropertiesById(clusterId))));

        return ResponseEntity.ok(new ClusterModelAssembler().toCollectionModel(clusterModels)
                .add(linkTo(methodOn(ClusterController.class)
                        .index())
                        .withSelfRel())
        );
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<ClusterModel> getCluster(@PathVariable("clusterId") String id) {
        final ClusterProperties clusterProperties
                = applicationProperties.getClusterPropertiesById(id);

        final List<NodeModel> nodes
                = clusterManager.queryAllNodes(id);

        ClusterModel model = ClusterModel.from(clusterProperties);
        model.setNodes(new NodeModelAssembler(clusterProperties.getClusterType()).toCollectionModel(nodes));
        model.setLocalities(new LocalityModelAssembler(id, clusterProperties.getClusterType())
                .toCollectionModel(nodeLocalities(nodes)));

        return ResponseEntity.ok(new ClusterModelAssembler().toModel(model));
    }
}

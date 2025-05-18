package io.cockroachdb.pestcontrol.web.api.cluster;

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

import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;
import io.cockroachdb.pestcontrol.service.ClusterManager;
import io.cockroachdb.pestcontrol.web.front.MessageModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterRestController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ClusterModelAssembler clusterModelAssembler;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping()
    public ResponseEntity<CollectionModel<ClusterModel>> index() {
        List<ClusterModel> clusterModels = new ArrayList<>();

        applicationProperties.getClusterIds().forEach(clusterId -> {
            clusterModels.add(ClusterModel.from(applicationProperties.getClusterPropertiesById(clusterId)));
        });

        return ResponseEntity.ok(clusterModelAssembler.toCollectionModel(clusterModels)
                .add(linkTo(methodOn(ClusterRestController.class)
                        .index())
                        .withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClusterModel> getCluster(@PathVariable("id") String id) {
        ClusterProperties clusterProperties = applicationProperties.getClusterPropertiesById(id);

        LocalityModelAssembler localityModelAssembler
                = new LocalityModelAssembler(id, clusterProperties.getClusterType());

        final List<NodeModel> nodes = clusterManager.queryAllNodes(id);

        ClusterModel model = ClusterModel.from(clusterProperties);
        model.setNodes(new NodeModelAssembler(clusterProperties.getClusterType()).toCollectionModel(nodes));
        model.setLocalities(localityModelAssembler.toCollectionModel(nodeLocalities(nodes)));

        return ResponseEntity.ok(clusterModelAssembler.toModel(model));
    }

    private Collection<Locality> nodeLocalities(List<NodeModel> models) {
        Set<Locality> localities = new LinkedHashSet<>(); // keep insertion order
        models.stream()
                .map(NodeModel::getLocality)
                .forEach(localities::add);
        return localities;
    }

    @GetMapping("/{id}/version")
    public ResponseEntity<MessageModel> getVersion(@PathVariable("id") String id) {
        return ResponseEntity.ok(MessageModel.from(clusterManager.getClusterVersion(id))
                .add(linkTo(methodOn(ClusterRestController.class)
                        .getVersion(id))
                        .withSelfRel()));
    }
}

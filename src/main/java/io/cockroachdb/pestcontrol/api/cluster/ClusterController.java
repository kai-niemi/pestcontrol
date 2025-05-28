package io.cockroachdb.pestcontrol.api.cluster;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping
    public ResponseEntity<CollectionModel<ClusterModel>> index() {
        final List<ClusterModel> clusterModels = new ArrayList<>();

        applicationProperties.getClusterIds().forEach(clusterId ->
                clusterModels.add(ClusterModel.from(
                        applicationProperties.getClusterPropertiesById(clusterId))));

        return ResponseEntity.ok(new ClusterModelAssembler()
                .toCollectionModel(clusterModels)
                .add(linkTo(methodOn(ClusterController.class)
                        .index())
                        .withSelfRel())
        );
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<ClusterModel> getCluster(
            @PathVariable("clusterId") String id) {
        final ClusterProperties clusterProperties
                = applicationProperties.getClusterPropertiesById(id);

        ClusterModel resource = ClusterModel.from(clusterProperties);

        return ResponseEntity.ok(new ClusterModelAssembler()
                .toModel(resource));
    }
}

package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.model.ClusterProperties;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    @Autowired
    private ApplicationProperties applicationProperties;

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

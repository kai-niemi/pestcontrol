package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.manager.ClusterManager;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.operator.ClusterOperator;

@RestController
@RequestMapping("/api/cluster/admin")
public class ClusterAdminController {
    @Autowired
    private ClusterManager clusterManager;

    @PostMapping("/{clusterId}/locality/{tiers}/disrupt")
    public ResponseEntity<Void> disruptLocalityTier(@PathVariable("clusterId") String clusterId,
                                                    @PathVariable("tiers") String tiers) {
        ClusterOperator clusterOperator = clusterManager.getClusterOperator(clusterId);
        clusterOperator.disruptNodes(clusterManager.getClusterProperties(clusterId), tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clusterId}/locality/{tiers}/recover")
    public ResponseEntity<Void> recoverLocalityTier(@PathVariable("clusterId") String clusterId,
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

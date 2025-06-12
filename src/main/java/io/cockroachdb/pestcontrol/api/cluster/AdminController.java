package io.cockroachdb.pestcontrol.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.cluster.ClusterManager;
import io.cockroachdb.pestcontrol.cluster.ClusterOperator;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;

@RestController
@RequestMapping("/api/cluster/admin")
public class AdminController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/{clusterId}")
    public ResponseEntity<AdminModel> getAdmin(@PathVariable("clusterId") String clusterId) {
        AdminModel model = AdminModel.fromId(clusterId);
        ClusterType clusterType = clusterManager.getClusterProperties(clusterId).getClusterType();
        return ResponseEntity.ok(new AdminModelAssembler(clusterId, clusterType)
                .toModel(model));
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

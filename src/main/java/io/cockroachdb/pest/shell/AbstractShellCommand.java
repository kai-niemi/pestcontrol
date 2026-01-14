package io.cockroachdb.pest.shell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.shell.core.command.availability.Availability;

import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.util.PatternUtils;

public abstract class AbstractShellCommand {
    protected static Cluster SELECTED_CLUSTER;

    public Availability ifClusterSelected() {
        return Objects.isNull(SELECTED_CLUSTER)
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    public Availability ifCockroachCloudCluster() {
        return ifClusterSelected().isAvailable()
               && ClusterTypes.isCloud(SELECTED_CLUSTER.getClusterType())
                ? Availability.available()
                : Availability.unavailable("cluster type is not cockroach cloud!");
    }

    public Availability ifHostedCluster() {
        return ifClusterSelected().isAvailable()
               && ClusterTypes.isHosted(SELECTED_CLUSTER.getClusterType())
                ? Availability.available()
                : Availability.unavailable("cluster type is not hosted!");
    }

    public Availability ifHostedSecureCluster() {
        return ifClusterSelected().isAvailable()
               && ClusterTypes.isSecure(SELECTED_CLUSTER.getClusterType())
               && ClusterTypes.isHosted(SELECTED_CLUSTER.getClusterType())
                ? Availability.available()
                : Availability.unavailable("cluster type is not secure!");
    }

    public List<Integer> nodeIdRange(String nodes) {
        if (nodes.toLowerCase().startsWith("all")) {
            Cluster cluster = selectedCluster();
            return IntStream.rangeClosed(1, cluster.getNodes().size())
                    .boxed().collect(Collectors.toList());
        }
        return PatternUtils.parseIntRange(nodes);
    }

    public Cluster selectedCluster() {
        Objects.requireNonNull(SELECTED_CLUSTER, "Cluster not selected");
        return SELECTED_CLUSTER;
    }
}

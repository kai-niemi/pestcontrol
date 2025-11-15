package io.cockroachdb.pest.shell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.util.Assert;

import io.cockroachdb.pest.ProfileNames;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperators;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.Cluster;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.ClusterTypes;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
public abstract class AbstractCommand {
    private static Cluster SELECTED_CLUSTER;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ClusterOperators clusterOperators;

    @Autowired
    private Environment environment;

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

    public Availability ifSecureCluster() {
        return ifClusterSelected().isAvailable()
               && ClusterTypes.isSecure(SELECTED_CLUSTER.getClusterType())
                ? Availability.available()
                : Availability.unavailable("cluster type is not secure!");
    }

    public ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    public Cluster getSelectedCluster() {
        Objects.requireNonNull(SELECTED_CLUSTER, "Cluster not selected");
        return SELECTED_CLUSTER;
    }

    public ClusterOperator getClusterOperator(Cluster cluster) {
        if (List.of(environment.getActiveProfiles()).contains(ProfileNames.offline.name())) {
            return clusterOperators.getClusterOperator(ClusterType.local_insecure);
        }
        return clusterOperators.getClusterOperator(cluster.getClusterType());
    }

    protected void selectCluster(String clusterId) {
        if (!Objects.equals("none", clusterId)) {
            SELECTED_CLUSTER = applicationProperties.getClusterById(clusterId);
        } else {
            SELECTED_CLUSTER = null;
        }
    }

    protected Integer nodeId(String node) {
        Cluster cluster = getSelectedCluster();
        int id = Integer.parseInt(node);
        Assert.state(id > 0, "Node id must be > 0");
        Assert.state(id <= cluster.getNodes().size(),
                "Node id must be <= " + cluster.getNodes().size());
        return id;
    }

    protected List<Integer> nodeIdRange(String nodes) {
        if (nodes.toLowerCase().startsWith("all")) {
            Cluster cluster = getSelectedCluster();
            return IntStream.rangeClosed(1, cluster.getNodes().size())
                    .boxed().collect(Collectors.toList());
        }
        return PatternUtils.parseIntRange(nodes);
    }
}

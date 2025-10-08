package io.cockroachdb.pest.shell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.util.Assert;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.util.PatternUtils;

@ShellComponent
public abstract class AbstractCommand {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ClusterManager clusterManager;

    @Autowired
    protected ApplicationProperties applicationProperties;

    protected static ClusterProperties CLUSTER_PROPERTIES;

    public Availability ifClusterSelected() {
        return Objects.isNull(CLUSTER_PROPERTIES)
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    public ClusterProperties getClusterProperties() {
        if (Objects.isNull(CLUSTER_PROPERTIES)) {
            throw new IllegalStateException("Cluster ID not specified");
        }
        return CLUSTER_PROPERTIES;
    }

    public ClusterOperator getClusterOperator() {
        ClusterProperties clusterProperties = getClusterProperties();
        return clusterManager.getClusterOperator(clusterProperties.getClusterId());
    }

    protected Integer nodeId(String node) {
        ClusterProperties clusterProperties = getClusterProperties();
        int id = Integer.parseInt(node);
        Assert.state(id > 0, "Node id must be > 0");
        Assert.state(id <= clusterProperties.getNodes().size(),
                "Node id must be <= " + clusterProperties.getNodes().size());
        return id;
    }

    protected List<Integer> nodeIdRange(String nodes) {
        if (nodes.toLowerCase().startsWith("all")) {
            ClusterProperties clusterProperties = getClusterProperties();
            return IntStream.rangeClosed(1, clusterProperties.getNodes().size())
                    .boxed().collect(Collectors.toList());
        }
        return PatternUtils.parseIntRange(nodes);
    }
}

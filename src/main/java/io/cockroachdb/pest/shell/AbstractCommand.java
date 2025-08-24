package io.cockroachdb.pest.shell;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;

@ShellComponent
public abstract class AbstractCommand {
    protected static final ThreadLocal<ClusterProperties> CLUSTER_ID_SELECTION = ThreadLocal.withInitial(() -> null);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ClusterManager clusterManager;

    @Autowired
    protected ApplicationProperties applicationProperties;

    public Availability ifClusterSelected() {
        return Objects.isNull(CLUSTER_ID_SELECTION.get())
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    public ClusterProperties getClusterSettings() {
        if (Objects.isNull(CLUSTER_ID_SELECTION.get())) {
            throw new IllegalStateException("Cluster ID not specified");
        }
        return CLUSTER_ID_SELECTION.get();
    }
}

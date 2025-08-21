package io.cockroachdb.pest.shell;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;

import io.cockroachdb.pest.cluster.ClusterManager;
import io.cockroachdb.pest.model.ApplicationSettings;
import io.cockroachdb.pest.model.ClusterSettings;

@ShellComponent
public abstract class AbstractCommand {
    protected static final ThreadLocal<ClusterSettings> CLUSTER_ID_SELECTION = ThreadLocal.withInitial(() -> null);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ClusterManager clusterManager;

    @Autowired
    protected ApplicationSettings applicationSettings;

    public Availability ifClusterSelected() {
        return Objects.isNull(CLUSTER_ID_SELECTION.get())
                ? Availability.unavailable("No cluster ID selected")
                : Availability.available();
    }

    public ClusterSettings getClusterProperties() {
        if (Objects.isNull(CLUSTER_ID_SELECTION.get())) {
            throw new IllegalStateException("Cluster ID not specified");
        }
        return CLUSTER_ID_SELECTION.get();
    }
}

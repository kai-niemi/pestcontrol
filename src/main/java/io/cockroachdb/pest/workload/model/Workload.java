package io.cockroachdb.pest.workload.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.cockroachdb.pest.util.TimeUtils;
import io.cockroachdb.pest.web.LinkRelations;

@Relation(itemRelation = LinkRelations.WORKLOAD_REL)
//        collectionRelation = LinkRelations.WORKLOAD_INDEX_REL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workload extends RepresentationModel<Workload> {
    private final String clusterId;

    private final Integer id;

    private final String description;

    private final Metrics metrics;

    private final LinkedList<Problem> problems = new LinkedList<>();

    private final Instant startTime;

    private final Duration duration;

    private boolean failed;

    @JsonIgnore
    private CompletableFuture<?> future;

    public Workload(String clusterId,
                    Integer id,
                    String description,
                    Metrics metrics,
                    Duration duration) {
        this.clusterId = clusterId;
        this.id = id;
        this.description = description;
        this.metrics = metrics;
        this.startTime = Instant.now();
        this.duration = duration;
    }

    public String getClusterId() {
        return clusterId;
    }

    public Integer getId() {
        return id;
    }

    public WorkloadStatus getStatus() {
        if (failed) {
            return WorkloadStatus.failed;
        } else if (isRunning()) {
            return WorkloadStatus.running;
        } else if (isCancelled()) {
            return WorkloadStatus.cancelled;
        } else {
            return WorkloadStatus.completed;
        }
    }

    public Workload setFuture(CompletableFuture<?> future) {
        this.future = future;
        return this;
    }

    public Workload setFailed(boolean failed) {
        this.failed = failed;
        return this;
    }

    public Workload addProblem(Problem problem) {
        if (problems.size() >= 50) {
            problems.removeLast();
        }
        problems.addFirst(problem);
        return this;
    }

    public String getTitle() {
        return description;
    }

    public List<Problem> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    public Metrics getMetrics() {
        return isRunning() ? metrics : Metrics.copy(metrics);
    }

    public String getRemainingTime() {
        return isRunning() ? TimeUtils.durationToDisplayString(getRemainingDuration()) : "-";
    }

    public Duration getRemainingDuration() {
        Duration remainingDuration = Duration.between(Instant.now(), startTime.plus(duration));
        return !remainingDuration.isNegative() ? remainingDuration : Duration.ofSeconds(0);
    }

    public boolean isRunning() {
        return !future.isDone();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean cancel() {
        return future.cancel(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Workload workload = (Workload) o;
        return Objects.equals(id, workload.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Workload{" +
               "clusterId='" + clusterId + '\'' +
               ", description='" + description + '\'' +
               ", duration=" + duration +
               ", failed=" + failed +
               ", id=" + id +
               ", startTime=" + startTime +
               "} " + super.toString();
    }
}

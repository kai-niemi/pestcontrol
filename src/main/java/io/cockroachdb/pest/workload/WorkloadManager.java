package io.cockroachdb.pest.workload;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.cockroachdb.pest.workload.model.Metrics;
import io.cockroachdb.pest.workload.model.Workload;

@Component
public class WorkloadManager {
    private final List<ClusterWorkload> clusterWorkloads
            = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private WorkloadExecutor workloadExecutor;

    public void submitWorkload(String clusterId,
                               Duration duration,
                               Runnable task,
                               String description) {
        get(clusterId)
                .add(workloadExecutor.submitTask(clusterId, duration, task, description));
    }

    public Set<String> clusterIds() {
        return clusterWorkloads.stream().map(ClusterWorkload::getClusterId).collect(Collectors.toSet());
    }

    public List<Workload> getWorkloads(String clusterId) {
        return getWorkloads(clusterId, workload -> true);
    }

    public List<Workload> getWorkloads(String clusterId, Predicate<Workload> predicate) {
        return get(clusterId).findAll(predicate);
    }

    public Workload findById(String clusterId, Integer id) {
        return get(clusterId)
                .findById(id);
    }

    public void deleteById(String clusterId, Integer id) {
        get(clusterId)
                .deleteById(id);
    }

    public void cancelAll(String clusterId) {
        get(clusterId).cancelAll();
    }

    public void deleteAll(String clusterId) {
        get(clusterId).clearAll();
    }

    public void takeSnapshot(Duration samplePeriod) {
        clusterWorkloads.parallelStream()
                .forEach(clusterWorkers ->
                        clusterWorkers.updateDataPoints(samplePeriod));
    }

    public void clearDataPoints(String clusterId) {
        get(clusterId).clearDataPoints();
    }

    /**
     * Return time series sample interval (x-axis)
     */
    public List<Instant> getTimeSeriesInterval(String clusterId) {
        return get(clusterId).getTimeSeriesInterval();
    }

    /**
     * Return time series sample values/metrics per workload (y-axis)
     *
     * @param id workload id
     */
    public List<Metrics> getTimeSeriesValues(String clusterId, Integer id) {
        return get(clusterId).getTimeSeriesValues(id);
    }

    /**
     * Return aggregated time series for all workloads.
     *
     * @param clusterId static cluster id
     * @return aggregated time series metrics
     */
    public Metrics getMetricsAggregate(String clusterId) {
        return get(clusterId).getTimeSeriesAggregate();
    }

    private ClusterWorkload get(String clusterId) {
        Assert.notNull(clusterId, "clusterId is null");

        Optional<ClusterWorkload> optionalClusterWorkload = clusterWorkloads.stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst();
        ClusterWorkload clusterWorkload;
        if (optionalClusterWorkload.isPresent()) {
            clusterWorkload = optionalClusterWorkload.get();
        } else {
            clusterWorkload = new ClusterWorkload(clusterId);
            clusterWorkloads.add(clusterWorkload);
        }
        return clusterWorkload;
    }

    public List<Map<String, Object>> getDataPoints(String clusterId, Function<Metrics, Double> mapper) {
        final List<Map<String, Object>> columnData = new ArrayList<>();

        {
            final Map<String, Object> headerElement = new HashMap<>();
            List<Long> labels =
                    getTimeSeriesInterval(clusterId)
                            .stream()
                            .map(Instant::toEpochMilli)
                            .toList();
            headerElement.put("data", labels.toArray());
            columnData.add(headerElement);
        }

        getWorkloads(clusterId, workload -> true)
                .forEach(workload -> {
                    Map<String, Object> dataElement = new HashMap<>();

                    List<Double> data =
                            getTimeSeriesValues(clusterId, workload.getId())
                                    .stream()
//                            .filter(metric -> !metric.isExpired(workload.getStopTime()))
                                    .map(mapper)
                                    .toList();

                    dataElement.put("id", workload.getId());
                    dataElement.put("name", "%s (%d)".formatted(workload.getTitle(), workload.getId()));
                    dataElement.put("data", data.toArray());

                    columnData.add(dataElement);
                });

        return columnData;
    }
}


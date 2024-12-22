package io.cockroachdb.pestcontrol.workload;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.workload.model.Workload;
import io.cockroachdb.pestcontrol.workload.support.ClusterWorkload;
import io.cockroachdb.pestcontrol.workload.model.Metrics;
import io.cockroachdb.pestcontrol.workload.support.WorkloadExecutor;

@Component
public class WorkloadManager {
    /**
     * Map cluster id to active workload workers.
     */
    private final List<ClusterWorkload> clusterWorkloads = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private WorkloadExecutor workloadExecutor;

    public void submitWorkload(String clusterId,
                               Duration duration,
                               Runnable task,
                               String description) {
        Workload workload = workloadExecutor.submitTask(clusterId, duration, task, description);
        findClusterWorkload(clusterId).add(workload);
    }

    private ClusterWorkload findClusterWorkload(String clusterId) {
        return clusterWorkloads.stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElse(new ClusterWorkload(clusterId));
    }

    public List<Workload> getWorkers(String clusterId) {
        return findClusterWorkload(clusterId)
                .findAll();
    }

    public Workload findById(String clusterId, Integer id) {
        return findClusterWorkload(clusterId)
                .findById(id);
    }

    public void deleteById(String clusterId, Integer id) {
        findClusterWorkload(clusterId)
                .deleteById(id);
    }

    public void cancelAll(String clusterId) {
        findClusterWorkload(clusterId).cancelAll();
    }

    public void deleteAll(String clusterId) {
        findClusterWorkload(clusterId).clearAll();
    }

    public void updateDataPoints(Duration samplePeriod) {
        clusterWorkloads.parallelStream()
                .forEach(clusterWorkers -> clusterWorkers.updateDataPoints(samplePeriod));
    }

    public void clearDataPoints(String clusterId) {
        findClusterWorkload(clusterId).clearDataPoints();
    }

    /**
     * Return time series sample interval (x-axis)
     */
    public List<Instant> getTimeSeriesInterval(String clusterId) {
        return findClusterWorkload(clusterId).getTimeSeriesInterval();
    }

    /**
     * Return time series sample values/metrics per workload (y-axis)
     *
     * @param id workload id
     */
    public List<Metrics> getTimeSeriesValues(String clusterId, Integer id) {
        return findClusterWorkload(clusterId).getTimeSeriesValues(id);
    }

    /**
     * Return aggregated time series for all workloads.
     *
     * @param clusterId static cluster id
     * @return aggregated time series metrics
     */
    public Metrics getAggregatedMetrics(String clusterId) {
        return findClusterWorkload(clusterId).getTimeSeriesAggregate();
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

        getWorkers(clusterId).forEach(workload -> {
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


package io.cockroachdb.pest.workload;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import io.cockroachdb.pest.cluster.ResourceNotFoundException;
import io.cockroachdb.pest.workload.model.DataPoint;
import io.cockroachdb.pest.workload.model.Metrics;
import io.cockroachdb.pest.workload.model.MetricsDataPoint;
import io.cockroachdb.pest.workload.model.Workload;

/**
 * Background workers and metric data points for a single cluster.
 */
public class ClusterWorkload {
    private final String clusterId;

    private final List<Workload> workloads = Collections.synchronizedList(new ArrayList<>());

    private final List<MetricsDataPoint> dataPoints = Collections.synchronizedList(new ArrayList<>());

    ClusterWorkload(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void add(Workload workload) {
        workloads.add(workload);
    }

    public void deleteById(Integer id) {
        Workload workload = findById(id);
        if (workload.isRunning()) {
            throw new IllegalStateException("Workload is running: " + id);
        }

        if (workload.isRunning()) {
            throw new IllegalStateException("Workload is running: " + id);
        }
        workloads.remove(workload);
    }

    public List<Workload> findAll(Predicate<Workload> predicate) {
        return workloads.stream().filter(predicate).toList();
    }

    public Workload findById(Integer id) {
        return workloads
                .stream()
                .filter(worker -> Objects.equals(worker.getId(), id))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException("No workload with id: " + id));
    }

    public void cancelAll() {
        workloads.stream()
                .filter(Workload::isRunning)
                .forEach(Workload::cancel);
    }

    public void clearAll() {
        cancelAll();
        workloads.clear();
    }

    public List<Instant> getTimeSeriesInterval() {
        return dataPoints.stream().map(DataPoint::getInstant).toList();
    }

    public List<Metrics> getTimeSeriesValues(Integer id) {
        List<Metrics> metrics = new ArrayList<>();
        dataPoints.forEach(dataPoint -> metrics.add(
                dataPoint.getValue(id, Metrics.empty())));
        return metrics;
    }

    public Metrics getTimeSeriesAggregate() {
        List<Metrics> metrics = workloads.stream()
                .map(Workload::getMetrics).toList();
        return Metrics.builder()
                .withUpdateTime(Instant.now())
                .withMeanTimeMillis(metrics.stream()
                        .mapToDouble(Metrics::getMeanTimeMillis).average().orElse(0))
                .withOps(metrics.stream().mapToDouble(Metrics::getOpsPerSec).sum(),
                        metrics.stream().mapToDouble(Metrics::getOpsPerMin).sum())
                .withP50(metrics.stream().mapToDouble(Metrics::getP50).average().orElse(0))
                .withP90(metrics.stream().mapToDouble(Metrics::getP90).average().orElse(0))
                .withP95(metrics.stream().mapToDouble(Metrics::getP95).average().orElse(0))
                .withP99(metrics.stream().mapToDouble(Metrics::getP99).average().orElse(0))
                .withP999(metrics.stream().mapToDouble(Metrics::getP999).average().orElse(0))
                .withMeanTimeMillis(metrics.stream().mapToDouble(Metrics::getMeanTimeMillis).average().orElse(0))
                .withSuccessful(metrics.stream().mapToInt(Metrics::getSuccess).sum())
                .withFails(metrics.stream().mapToInt(Metrics::getTransientFail).sum(),
                        metrics.stream().mapToInt(Metrics::getNonTransientFail).sum())
                .build();
    }

    public void updateDataPoints(Duration samplePeriod) {
        // Purge old data points older than sample period
        dataPoints.removeIf(item -> item.getInstant()
                .isBefore(Instant.now().minusSeconds(samplePeriod.toSeconds())));

        // Add new datapoint by sampling all workload metrics
        MetricsDataPoint dataPoint = new MetricsDataPoint(Instant.now());

        // Add datapoint if still running
        workloads.stream()
                .filter(Workload::isRunning)
                .forEach(workload -> dataPoint.putValue(workload.getId(), workload.getMetrics()));

        dataPoints.add(dataPoint);
    }

    public void clearDataPoints() {
        dataPoints.clear();
    }
}

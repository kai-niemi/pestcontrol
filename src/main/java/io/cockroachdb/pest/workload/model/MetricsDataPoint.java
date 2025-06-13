package io.cockroachdb.pest.workload.model;

import java.time.Instant;

public class MetricsDataPoint extends DataPoint<Integer, Metrics> {
    public MetricsDataPoint(Instant instant) {
        super(instant);
    }

    @Override
    public void putValue(Integer id, Metrics metric) {
        super.putValue(id, Metrics.builder()
                .withSuccessful(metric.getSuccess())
                .withFails(metric.getTransientFail(), metric.getNonTransientFail())
                .withOps(metric.getOpsPerSec(), metric.getOpsPerMin())
                .withP50(metric.getP50())
                .withP90(metric.getP90())
                .withP95(metric.getP95())
                .withP99(metric.getP99())
                .withP999(metric.getP999())
                .withUpdateTime(metric.getUpdateTime())
                .withMeanTimeMillis(metric.getMeanTimeMillis())
                .build()
        );
    }
}


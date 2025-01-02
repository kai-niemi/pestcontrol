package io.cockroachdb.pc.workload.model;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metrics {
    private static final int MAX_AGE_SECONDS = 10;

    public static Metrics empty() {
        return new Metrics();
    }

    public static Metrics copy(Metrics from) {
        Metrics m = new Metrics();
        m.updateTime = from.updateTime;
        m.success = from.getSuccess();
        m.transientFail = from.getTransientFail();
        m.nonTransientFail = from.getNonTransientFail();
        return m;
    }

    private static double percentile(List<Double> sortedDurations, double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException(">=0 N <=1");
        }
        if (!sortedDurations.isEmpty()) {
            int index = (int) Math.ceil(percentile * sortedDurations.size());
            return sortedDurations.get(index - 1);
        }
        return 0;
    }

    @JsonIgnore
    private final LinkedList<Pair<Instant, Pair<Duration, Integer>>> fifoBuffer = new LinkedList<>();

    private final Instant startTime = Instant.now();

    private Instant updateTime;

    private int success;

    private int transientFail;

    private int nonTransientFail;

    private double opsPerSec;

    private double opsPerMin;

    private double meanTimeMillis;

    private double p50;

    private double p90;

    private double p95;

    private double p99;

    private double p999;

    private Metrics() {
        this.updateTime = Instant.now();
    }

    public void markSuccess(Duration duration) {
        success++;
        update(duration);
    }

    public void markFail(Duration duration, boolean isTransient) {
        if (isTransient) {
            transientFail++;
        } else {
            nonTransientFail++;
        }

        update(duration);
    }

    private void update(Duration duration) {
        updateTime = Instant.now();

        fifoBuffer.add(Pair.of(updateTime, Pair.of(duration, success)));

        // Purge items by time range
        fifoBuffer.removeIf(item -> item.getFirst()
                .isBefore(updateTime.minusSeconds(MAX_AGE_SECONDS)));

        // Latency percentiles and mean time
        {
            List<Double> sortedDurationMillis = fifoBuffer
                    .stream()
                    .map(p -> p.getSecond().getFirst())
                    .toList()
                    .stream()
                    .mapToDouble(Duration::toMillis)
                    .sorted()
                    .boxed()
                    .toList();
            p50 = percentile(sortedDurationMillis, .5);
            p90 = percentile(sortedDurationMillis, .9);
            p95 = percentile(sortedDurationMillis, .95);
            p99 = percentile(sortedDurationMillis, .99);
            p999 = percentile(sortedDurationMillis, .999);

            meanTimeMillis = sortedDurationMillis
                    .stream()
                    .mapToDouble(value -> value)
                    .average()
                    .orElse(0);
        }

        // Ops per time unit
        {
            List<Instant> samples = fifoBuffer
                    .stream()
                    .map(Pair::getFirst)
                    .filter(instant -> instant.isAfter(updateTime.minusSeconds(MAX_AGE_SECONDS)))
                    .toList();
            Instant oldestTime = samples.stream()
                    .findFirst()
                    .orElse(updateTime);

            opsPerSec = samples.size() /
                        Math.max(1, Duration.between(oldestTime, updateTime).toMillis() / 1000.0);

            opsPerMin = opsPerSec * 60;
        }
    }

    public double getRemainingTimeSeconds() {
        return Duration.between(startTime, Instant.now()).toMillis() / 1000.0;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public boolean isExpired() {
        return false;
    }

    public int getSuccess() {
        return success;
    }

    public int getTransientFail() {
        return transientFail;
    }

    public int getNonTransientFail() {
        return nonTransientFail;
    }

    public double getMeanTimeMillis() {
        return meanTimeMillis;
    }

    public double getOpsPerSec() {
        return opsPerSec;
    }

    public double getOpsPerMin() {
        return opsPerMin;
    }

    public double getP50() {
        return p50;
    }

    public double getP90() {
        return p90;
    }

    public double getP95() {
        return p95;
    }

    public double getP99() {
        return p99;
    }

    public double getP999() {
        return p999;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Metrics instance = new Metrics();

        private Builder() {
        }

        public Builder withUpdateTime(Instant updateTime) {
            instance.updateTime = updateTime;
            return this;
        }

        public Builder withMeanTimeMillis(double meanTimeMillis) {
            instance.meanTimeMillis = meanTimeMillis;
            return this;
        }

        public Builder withOps(double opsPerSec, double opsPerMin) {
            instance.opsPerSec = opsPerSec;
            instance.opsPerMin = opsPerMin;
            return this;
        }

        public Builder withP50(double p50) {
            instance.p50 = p50;
            return this;
        }

        public Builder withP90(double p90) {
            instance.p90 = p90;
            return this;
        }

        public Builder withP95(double p9999) {
            instance.p95 = p9999;
            return this;
        }

        public Builder withP999(double p999) {
            instance.p999 = p999;
            return this;
        }

        public Builder withP99(double p99) {
            instance.p99 = p99;
            return this;
        }

        public Builder withSuccessful(int successful) {
            instance.success = successful;
            return this;
        }

        public Builder withFails(int transientFail, int nonTransientFail) {
            instance.transientFail = transientFail;
            instance.nonTransientFail = nonTransientFail;
            return this;
        }

        public Metrics build() {
            Assert.notNull(instance.updateTime, "time is null");
            return instance;
        }
    }
}

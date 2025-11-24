package io.cockroachdb.pest.workload.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;

public class TimeSeries {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<DoubleDataPoint> dataPoints = Collections.synchronizedList(new ArrayList<>());

    private final List<Meter.Id> meterIds = new ArrayList<>();

    private Duration samplePeriod = Duration.ofSeconds(300);

    private final String name;

    private final MeterRegistry meterRegistry;

    private final Supplier<List<Search>> searchSupplier;

    public TimeSeries(String name,
                      MeterRegistry meterRegistry,
                      Supplier<List<Search>> searchSupplier) {
        this.name = name;
        this.meterRegistry = meterRegistry;
        this.searchSupplier = searchSupplier;
    }

    @SuppressWarnings("resource")
    @PostConstruct
    public void registerMeters() {
        // Race condition prevents searching for meters directly
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            searchSupplier.get().forEach(search -> {
                Meter meter = search.meter();
                if (meter == null) {
                    logger.warn("Meter not found - check config for time series '{}'", name);
                } else {
                    meterIds.add(meter.getId());
                }
            });
        }, 5, TimeUnit.SECONDS);
    }

    public void setSamplePeriod(Duration samplePeriod) {
        this.samplePeriod = samplePeriod;
    }

    public void takeSnapshot() {
        // Purge old data points older than sample period
        dataPoints.removeIf(dataPoint -> dataPoint.getInstant()
                .isBefore(Instant.now().minusSeconds(samplePeriod.toSeconds())));

        // Add new datapoint by sampling all defined metrics
        DoubleDataPoint dataPoint = new DoubleDataPoint(Instant.now());

        meterRegistry.getMeters()
                .stream()
                .filter(meter -> {
                    final Meter.Id id = meter.getId();
                    return meterIds.stream().anyMatch(x -> x.equals(id));
                })
                .forEach(meter -> {
                    final Meter.Id id = meter.getId();
                    meter.measure()
                            .forEach(measurement -> {
                                dataPoint.putValue(id.getName(), measurement.getValue());
                            });
                });

        dataPoints.add(dataPoint);
    }

    public List<Map<String, Object>> getDataPoints() {
        final List<Map<String, Object>> columnData = new ArrayList<>();

        {
            List<Long> labels =
                    dataPoints.stream()
                            .map(DoubleDataPoint::getInstant)
                            .toList()
                            .stream()
                            .map(Instant::toEpochMilli)
                            .toList();

            Map<String, Object> headerElement = new HashMap<>();
            headerElement.put("data", labels.toArray());

            columnData.add(headerElement);
        }

        meterRegistry
                .getMeters()
                .stream()
                .filter(meter -> {
                    final Meter.Id id = meter.getId();
                    return meterIds.stream().anyMatch(pair -> pair.equals(id));
                })
                .forEach(meter -> {
                    final Meter.Id id = meter.getId();

                    List<Double> data = dataPoints
                            .stream()
                            .filter(dataPoint -> !dataPoint.isExpired())
                            .map(dataPoint -> dataPoint.getValue(id.getName(), .0))
                            .toList();

                    Map<String, Object> dataElement = new HashMap<>();
                    dataElement.put("data", data.toArray());
                    dataElement.put("id", id.getName());
                    dataElement.put("name", "%s".formatted(id.getDescription()));

                    columnData.add(dataElement);
                });

        return columnData;
    }
}

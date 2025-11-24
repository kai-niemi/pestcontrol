package io.cockroachdb.pest.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pest.workload.model.TimeSeries;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class InstrumentationConfiguration {
    @SuppressWarnings("DataFlowIssue")
    @Bean
    public TimeSeries storageTimeSeries(@Autowired MeterRegistry registry) {
        return new TimeSeries("storage", registry, () -> List.of(
                registry.find("disk.free"),
                registry.find("disk.total")
        ));
    }

    @SuppressWarnings("DataFlowIssue")
    @Bean
    public TimeSeries heapTimeSeries(@Autowired MeterRegistry registry) {
        return new TimeSeries("heap", registry, () -> List.of(
                registry.find("jvm.memory.max"),
//                registry.find("jvm.memory.used"),
                registry.find("jvm.memory.committed")
        ));
    }

    @SuppressWarnings("DataFlowIssue")
    @Bean
    public TimeSeries threadPoolTimeSeries(@Autowired MeterRegistry registry) {
        return new TimeSeries("threads", registry, () -> List.of(
                registry.find("jvm.threads.live"),
                registry.find("jvm.threads.peak")
        ));
    }

    @SuppressWarnings("DataFlowIssue")
    @Bean
    public TimeSeries cpuTimeSeries(@Autowired MeterRegistry registry) {
        return new TimeSeries("system", registry, () -> List.of(
                registry.find("process.cpu.usage"),
                registry.find("system.cpu.usage")
        ));
    }
}

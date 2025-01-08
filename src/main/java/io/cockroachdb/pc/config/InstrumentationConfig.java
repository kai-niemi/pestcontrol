package io.cockroachdb.pc.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cockroachdb.pc.workload.model.TimeSeries;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class InstrumentationConfig {
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

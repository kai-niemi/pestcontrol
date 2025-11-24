package io.cockroachdb.pest.workload;

import org.springframework.context.ApplicationEvent;

public class WorkloadUpdatedEvent extends ApplicationEvent {
    public WorkloadUpdatedEvent(Object source) {
        super(source);
    }
}

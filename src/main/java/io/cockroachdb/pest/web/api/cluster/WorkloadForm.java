package io.cockroachdb.pest.web.api.cluster;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.cockroachdb.pest.workload.repository.WorkloadType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkloadForm extends RepresentationModel<WorkloadForm> {
    @NotNull
    private WorkloadType workerType;

    @NotNull
    @Pattern(regexp = "^[0-2][0-3]:[0-5][0-9]$")
    private String duration;

    @NotNull
    private Integer count = 1;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public WorkloadType getWorkloadType() {
        return workerType;
    }

    public void setWorkloadType(WorkloadType workerType) {
        this.workerType = workerType;
    }

    public @NotNull Integer getCount() {
        return count;
    }

    public void setCount(@NotNull Integer count) {
        this.count = count;
    }
}

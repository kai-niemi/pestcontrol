package io.cockroachdb.pestcontrol.api.chart;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.MessageModel;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import io.cockroachdb.pestcontrol.web.AbstractSessionController;
import io.cockroachdb.pestcontrol.web.ClusterModel;
import io.cockroachdb.pestcontrol.workload.WorkloadManager;
import io.cockroachdb.pestcontrol.workload.model.Metrics;
import io.cockroachdb.pestcontrol.workload.model.Workload;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * ChartJS workload (session) data paint callback methods.
 */
@RestController
@RequestMapping(value = "/api/chart/workload")
public class WorkloadChartController extends AbstractSessionController {
    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void takeDataPointSnapshots() {
        workloadManager.takeSnapshot(Duration.ofSeconds(applicationProperties.getSamplePeriodSeconds()));
    }

    @GetMapping
    public ResponseEntity<MessageModel> index(@SessionAttribute("helper") ClusterModel clusterModel) {
        MessageModel index = new MessageModel();
        index.add(linkTo(methodOn(getClass())
                .index(clusterModel))
                .withSelfRel());
        index.add(linkTo(methodOn(getClass())
                .getWorkloadItems(clusterModel))
                .withRel(LinkRelations.DATA_POINTS_REL));
        index.add(linkTo(methodOn(getClass())
                .getWorkloadMetrics(clusterModel))
                .withRel(LinkRelations.DATA_POINTS_REL));
        return ResponseEntity.ok(index);
    }

    //
    // Special workload aggregation metrics (not using micrometer)
    //

    @GetMapping("/items")
    public @ResponseBody List<Workload> getWorkloadItems(
            @SessionAttribute("helper") ClusterModel clusterModel) {
        return workloadManager.getWorkloads(clusterModel.getId());
    }

    @GetMapping("/metrics")
    public @ResponseBody Metrics getWorkloadMetrics(
            @SessionAttribute("helper") ClusterModel clusterModel) {
        return workloadManager.getMetricsAggregate(clusterModel.getId());
    }

    @GetMapping(value = "/data-points/p99",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getWorkloadDataPointsP99(
            @SessionAttribute("helper") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getId(), Metrics::getP99);
    }

    @GetMapping(value = "/data-points/p999",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getWorkloadDataPointsP999(
            @SessionAttribute("helper") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getId(), Metrics::getP999);
    }

    @GetMapping(value = "/data-points/tps",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getWorkloadDataPointsTPS(
            @SessionAttribute("helper") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getId(), Metrics::getOpsPerSec);
    }
}

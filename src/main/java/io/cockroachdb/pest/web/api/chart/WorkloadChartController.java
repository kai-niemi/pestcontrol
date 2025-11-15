package io.cockroachdb.pest.web.api.chart;

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

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.web.AbstractSessionController;
import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.web.api.ClusterModel;
import io.cockroachdb.pest.web.api.MessageModel;
import io.cockroachdb.pest.workload.WorkloadManager;
import io.cockroachdb.pest.workload.model.Metrics;
import io.cockroachdb.pest.workload.model.Workload;
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
        workloadManager.takeSnapshot(Duration.ofSeconds(15));
    }

    @GetMapping
    public ResponseEntity<MessageModel> index(@SessionAttribute("model") ClusterModel clusterModel) {
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
            @SessionAttribute("model") ClusterModel clusterModel) {
        return workloadManager.getWorkloads(clusterModel.getClusterId());
    }

    @GetMapping("/metrics")
    public @ResponseBody Metrics getWorkloadMetrics(
            @SessionAttribute("model") ClusterModel clusterModel) {
        return workloadManager.getMetricsAggregate(clusterModel.getClusterId());
    }

    @GetMapping(value = "/data-points/p99",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getWorkloadDataPointsP99(
            @SessionAttribute("model") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getClusterId(), Metrics::getP99);
    }

    @GetMapping(value = "/data-points/p999",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getWorkloadDataPointsP999(
            @SessionAttribute("model") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getClusterId(), Metrics::getP999);
    }

    @GetMapping(value = "/data-points/tps",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getWorkloadDataPointsTPS(
            @SessionAttribute("model") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getClusterId(), Metrics::getOpsPerSec);
    }
}

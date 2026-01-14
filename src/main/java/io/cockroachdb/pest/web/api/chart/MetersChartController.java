package io.cockroachdb.pest.web.api.chart;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.web.api.MessageModel;
import io.cockroachdb.pest.util.TimeSeries;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * ChartJS generic data paint callback methods.
 */
@RestController
@RequestMapping(value = "/api/chart/meters")
public class MetersChartController {
    @Autowired
    @Qualifier("threadPoolTimeSeries")
    private TimeSeries threadPoolTimeSeries;

    @Autowired
    @Qualifier("cpuTimeSeries")
    private TimeSeries cpuTimeSeries;

    @Autowired
    @Qualifier("storageTimeSeries")
    private TimeSeries storageTimeSeries;

    @Autowired
    @Qualifier("heapTimeSeries")
    private TimeSeries heapTimeSeries;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void takeDataPointSnapshots() {
        threadPoolTimeSeries.takeSnapshot();
        cpuTimeSeries.takeSnapshot();
        storageTimeSeries.takeSnapshot();
        heapTimeSeries.takeSnapshot();
    }

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel());
        index.add(linkTo(methodOn(getClass())
                .getThreadPoolDataPoints())
                .withRel(LinkRelations.DATA_POINTS_REL));
        index.add(linkTo(methodOn(getClass())
                .getCpuDataPoints())
                .withRel(LinkRelations.DATA_POINTS_REL));
        return ResponseEntity.ok(index);
    }

    @GetMapping(value = "/data-points/thread-pool",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getThreadPoolDataPoints() {
        return threadPoolTimeSeries.getDataPoints();
    }

    @GetMapping(value = "/data-points/cpu",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getCpuDataPoints() {
        return cpuTimeSeries.getDataPoints();
    }

    @GetMapping(value = "/data-points/storage",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> storageDataPoints() {
        return storageTimeSeries.getDataPoints();
    }

    @GetMapping(value = "/data-points/heap",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> heapDataPoints() {
        return heapTimeSeries.getDataPoints();
    }
}

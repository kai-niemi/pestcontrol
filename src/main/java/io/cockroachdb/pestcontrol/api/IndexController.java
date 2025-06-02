package io.cockroachdb.pestcontrol.api;

import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pestcontrol.api.chart.TimeSeriesChartController;
import io.cockroachdb.pestcontrol.api.chart.WorkloadChartController;
import io.cockroachdb.pestcontrol.api.cluster.ClusterController;
import io.cockroachdb.pestcontrol.api.toxiproxy.ToxiproxyController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class IndexController {
    @GetMapping
    public ResponseEntity<MessageModel> index() {
        return ResponseEntity.ok(MessageModel.from("Welcome to PestControl API")
                .add(linkTo(methodOn(getClass())
                        .index())
                        .withSelfRel())
                .add(linkTo(methodOn(ClusterController.class)
                        .index())
                        .withRel(LinkRelations.CLUSTER_COLL_REL)
                        .withTitle("Cluster resource collection"))
                .add(linkTo(methodOn(ToxiproxyController.class)
                        .index())
                        .withRel(LinkRelations.TOXIPROXY_REL)
                        .withTitle("Toxiproxy control resource"))
                .add(linkTo(methodOn(TimeSeriesChartController.class)
                        .index())
                        .withRel(LinkRelations.CHART_COLL_REL)
                        .withTitle("Timeseries metrics for charts"))
                .add(linkTo(methodOn(WorkloadChartController.class)
                        .index(null))
                        .withRel(LinkRelations.CHART_COLL_REL)
                        .withTitle("Workload metrics for charts"))
                .add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                                .pathSegment("api", "actuator")
                                .buildAndExpand()
                                .toUriString())
                        .withRel(LinkRelations.ACTUATORS_REL)
                        .withTitle("Spring boot actuators")
                ));
    }

    @GetMapping("/browser")
    public RedirectView halExplorerPage() {
        String rootUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .pathSegment("api")
                .buildAndExpand()
                .toUriString();
        return new RedirectView("/browser/index.html#theme=Darkly&uri=" + rootUri);
    }
}

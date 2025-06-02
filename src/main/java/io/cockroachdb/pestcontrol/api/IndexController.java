package io.cockroachdb.pestcontrol.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pestcontrol.api.chart.TimeSeriesChartController;
import io.cockroachdb.pestcontrol.api.chart.WorkloadChartController;
import io.cockroachdb.pestcontrol.api.cluster.ClusterModel;
import io.cockroachdb.pestcontrol.api.cluster.ClusterModelAssembler;
import io.cockroachdb.pestcontrol.api.toxiproxy.ToxiproxyController;
import io.cockroachdb.pestcontrol.model.ApplicationProperties;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class IndexController {
    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping
    public ResponseEntity<CollectionModel<ClusterModel>> index() {
        final List<ClusterModel> clusterModels = new ArrayList<>();

        applicationProperties.getClusterIds().forEach(clusterId ->
                clusterModels.add(ClusterModel.from(
                        applicationProperties.getClusterPropertiesById(clusterId))));

        return ResponseEntity.ok(new ClusterModelAssembler()
                .toCollectionModel(clusterModels)
                .add(linkTo(methodOn(getClass())
                        .index())
                        .withSelfRel())
                .add(linkTo(methodOn(ToxiproxyController.class)
                        .index())
                        .withRel(LinkRelations.TOXIPROXY_REL)
                        .withTitle("Toxiproxy status and controls"))
                .add(linkTo(methodOn(TimeSeriesChartController.class)
                        .index())
                        .withRel(LinkRelations.CHART_COLL_REL)
                        .withTitle("VM chart metrics"))
                .add(linkTo(methodOn(WorkloadChartController.class)
                        .index(null))
                        .withRel(LinkRelations.CHART_COLL_REL)
                        .withTitle("Workload chart metrics"))
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

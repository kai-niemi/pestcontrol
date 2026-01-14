package io.cockroachdb.pest.web.api;

import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.cockroachdb.pest.web.LinkRelations;
import io.cockroachdb.pest.web.api.chart.MetersChartController;
import io.cockroachdb.pest.web.api.cluster.ClusterController;
import io.cockroachdb.pest.web.api.toxiproxy.ToxiproxyController;
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
                        .withRel(LinkRelations.CLUSTERS_REL)
                        .withTitle("Cluster collection resource"))
                .add(linkTo(methodOn(ToxiproxyController.class)
                        .index())
                        .withRel(LinkRelations.TOXIPROXY_REL)
                        .withTitle("Toxiproxy controls resource"))
                .add(linkTo(methodOn(MetersChartController.class)
                        .index())
                        .withRel(LinkRelations.CHARTS_REL)
                        .withTitle("Chart time-series resource"))
                .add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                                .pathSegment("api", "actuator")
                                .buildAndExpand()
                                .toUriString())
                        .withRel(LinkRelations.ACTUATORS_REL)
                        .withTitle("Spring boot actuators")
                )
        );
    }
}

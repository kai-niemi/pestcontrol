package io.cockroachdb.pc.web.api;

import java.io.IOException;

import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pc.web.api.chart.RestChartController;
import io.cockroachdb.pc.web.api.chart.RestChartWorkloadController;
import io.cockroachdb.pc.web.api.cluster.ClusterRestController;
import io.cockroachdb.pc.web.api.toxi.ToxiproxyRestController;
import io.cockroachdb.pc.web.push.MessageModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class IndexRestController {
    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = MessageModel.from("Pest Control Hypermedia API");

        index.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel()
                .withTitle("Hypermedia API index"));

        index.add(linkTo(methodOn(ClusterRestController.class)
                .getClusters())
                .withRel(LinkRelations.CLUSTER_LIST_REL)
                .withTitle("Cluster collection index"));

        index.add(linkTo(methodOn(ToxiproxyRestController.class)
                .index())
                .withRel(LinkRelations.TOXIPROXY_INDEX_REL)
                .withTitle("Toxiproxy index"));

        index.add(linkTo(methodOn(RestChartController.class)
                .index())
                .withRel(LinkRelations.CHARTS_REL)
                .withTitle("Chart metrics time series"));

        index.add(linkTo(methodOn(RestChartWorkloadController.class)
                .index(null))
                .withRel(LinkRelations.CHARTS_REL)
                .withTitle("Chart workload metrics time series (session bound)"));

        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("actuator")
                        .buildAndExpand()
                        .toUriString())
                .withRel(LinkRelations.ACTUATORS_REL)
                .withTitle("Spring boot actuators"));

        return ResponseEntity.ok(index);
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

/*
    @GetMapping("/rels/{name}")
    public ModelAndView relPage(
            @PathVariable("name") String name) throws IOException {
        ClassPathResource path = new ClassPathResource("/templates/rels/%s.md".formatted(name));
        if (!path.isReadable()) {
            path = new ClassPathResource("/templates/rels/default.md");
        }

        final List<Extension> extensions = List.of(TablesExtension.create());

        String html = HtmlRenderer.builder()
                .extensions(extensions)
                .build()
                .render(Parser.builder().extensions(extensions).build()
                        .parseReader(new InputStreamReader(path.getInputStream())));
        // Hack!
        html = html.replace("<table", "<table class='table'");
        return new ModelAndView("rel", "html", html);
    }
*/

    @GetMapping("/fake-error")
    public @ResponseBody ResponseEntity<MessageModel> errorOnGet() {
        throw new FakeException("Fake exception!", new IOException("I/O disturbance!"));
    }
}

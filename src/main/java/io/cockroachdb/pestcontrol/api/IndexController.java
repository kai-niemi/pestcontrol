package io.cockroachdb.pestcontrol.api;

import java.io.IOException;

import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pestcontrol.api.chart.ChartController;
import io.cockroachdb.pestcontrol.api.chart.ChartWorkloadController;
import io.cockroachdb.pestcontrol.api.cluster.ClusterController;
import io.cockroachdb.pestcontrol.api.machine.MachinesController;
import io.cockroachdb.pestcontrol.api.toxiproxy.ToxiproxyController;
import io.cockroachdb.pestcontrol.web.MessageModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class IndexController {
    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel resource = MessageModel.from("Pest Control Hypermedia API");

        resource.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel()
                .withTitle("API index"));
        resource.add(linkTo(methodOn(ClusterController.class)
                .index())
                .withRel(LinkRelations.CLUSTER_REL)
                .withTitle("Cluster liveness, status and admin"));
        resource.add(linkTo(methodOn(MachinesController.class)
                .index())
                .withRel(LinkRelations.NODES_REL)
                .withTitle("Nodes index"));
        resource.add(linkTo(methodOn(ToxiproxyController.class)
                .index())
                .withRel(LinkRelations.TOXIPROXY_INDEX_REL)
                .withTitle("Toxiproxy index"));
        resource.add(linkTo(methodOn(ChartController.class)
                .index())
                .withRel(LinkRelations.CHARTS_REL)
                .withTitle("Chart metrics"));
        resource.add(linkTo(methodOn(ChartWorkloadController.class)
                .index(null))
                .withRel(LinkRelations.CHARTS_REL)
                .withTitle("Chart metrics"));
        resource.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("api", "actuator")
                        .buildAndExpand()
                        .toUriString())
                .withRel(LinkRelations.ACTUATORS_REL)
                .withTitle("Spring boot actuators"));

        return ResponseEntity.ok(resource);
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

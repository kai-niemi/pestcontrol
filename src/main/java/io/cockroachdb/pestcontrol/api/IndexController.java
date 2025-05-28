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

import io.cockroachdb.pestcontrol.api.chart.VmChartController;
import io.cockroachdb.pestcontrol.api.chart.WorkloadChartController;
import io.cockroachdb.pestcontrol.api.cluster.ClusterController;
import io.cockroachdb.pestcontrol.api.toxiproxy.ToxiproxyController;
import io.cockroachdb.pestcontrol.api.workload.WorkloadController;
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
                .withRel(LinkRelations.CLUSTER_STATUS_REL)
                .withTitle("Cluster liveness, status and admin controls"));
        resource.add(linkTo(methodOn(WorkloadController.class)
                .index())
                .withRel(LinkRelations.WORKLOADS_REL)
                .withTitle("Cluster workloads"));
        resource.add(linkTo(methodOn(ToxiproxyController.class)
                .index())
                .withRel(LinkRelations.TOXIPROXY_REL)
                .withTitle("Toxiproxy status and controls"));
        resource.add(linkTo(methodOn(VmChartController.class)
                .index())
                .withRel(LinkRelations.CHARTS_REL)
                .withTitle("VM chart metrics"));
        resource.add(linkTo(methodOn(WorkloadChartController.class)
                .index(null))
                .withRel(LinkRelations.CHARTS_REL)
                .withTitle("Workload chart metrics"));
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

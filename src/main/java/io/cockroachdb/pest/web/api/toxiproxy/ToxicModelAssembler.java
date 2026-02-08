package io.cockroachdb.pest.web.api.toxiproxy;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicType;
import eu.rekawek.toxiproxy.model.toxic.Bandwidth;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import eu.rekawek.toxiproxy.model.toxic.LimitData;
import eu.rekawek.toxiproxy.model.toxic.ResetPeer;
import eu.rekawek.toxiproxy.model.toxic.Slicer;
import eu.rekawek.toxiproxy.model.toxic.SlowClose;
import eu.rekawek.toxiproxy.model.toxic.Timeout;
import jakarta.validation.constraints.NotNull;

import io.cockroachdb.pest.model.LinkRelations;
import io.cockroachdb.pest.web.model.ToxicModel;
import io.cockroachdb.pest.web.app.ToxiproxyDashboardController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

final class ToxicModelAssembler implements RepresentationModelAssembler<Toxic, ToxicModel> {
    private final String proxy;

    public ToxicModelAssembler(String proxy) {
        this.proxy = proxy;
    }

    @NotNull
    @Override
    public ToxicModel toModel(Toxic entity) {
        ToxicModel resource = new ToxicModel();
        resource.setName(entity.getName());
        resource.setStream(entity.getStream());
        resource.setToxicity(entity.getToxicity());

        if (entity.getClass().isAssignableFrom(Bandwidth.class)) {
            Bandwidth bandwidth = (Bandwidth) entity;
            resource.addAttribute("rate", bandwidth.getRate());
            resource.setType(ToxicType.BANDWIDTH);
        } else if (entity.getClass().isAssignableFrom(Latency.class)) {
            Latency toxic = (Latency) entity;
            resource.addAttribute("latency", toxic.getLatency());
            resource.addAttribute("jitter", toxic.getJitter());
            resource.setType(ToxicType.LATENCY);
        } else if (entity.getClass().isAssignableFrom(LimitData.class)) {
            LimitData toxic = (LimitData) entity;
            resource.addAttribute("bytes", toxic.getBytes());
            resource.setType(ToxicType.LIMIT_DATA);
        } else if (entity.getClass().isAssignableFrom(ResetPeer.class)) {
            ResetPeer toxic = (ResetPeer) entity;
            resource.addAttribute("timeout", toxic.getTimeout());
            resource.setType(ToxicType.RESET_PEER);
        } else if (entity.getClass().isAssignableFrom(Slicer.class)) {
            Slicer toxic = (Slicer) entity;
            resource.addAttribute("averageSize", toxic.getAverageSize());
            resource.addAttribute("delay", toxic.getDelay());
            resource.addAttribute("sizeVariation", toxic.getSizeVariation());
            resource.setType(ToxicType.SLICER);
        } else if (entity.getClass().isAssignableFrom(SlowClose.class)) {
            SlowClose toxic = (SlowClose) entity;
            resource.addAttribute("delay", toxic.getDelay());
            resource.setType(ToxicType.SLOW_CLOSE);
        } else if (entity.getClass().isAssignableFrom(Timeout.class)) {
            Timeout toxic = (Timeout) entity;
            resource.addAttribute("timeout", toxic.getTimeout());
            resource.setType(ToxicType.TIMEOUT);
        }

        resource.add(linkTo(methodOn(ToxiproxyController.class)
                .findProxyToxic(proxy, entity.getName()))
                .withSelfRel()
                .andAffordance(afford(methodOn(ToxiproxyController.class)
                        .deleteProxyToxic(proxy, entity.getName()))));

        resource.add(linkTo(methodOn(ToxiproxyController.class)
                .deleteProxyToxic(proxy, entity.getName()))
                .withRel(LinkRelations.DELETE_REL));

        resource.add(linkTo(methodOn(ToxiproxyDashboardController.class)
                .deleteProxyToxic(proxy, entity.getName()))
                .withRel(LinkRelations.DELETE_REL + "-redirect"));

        return resource;
    }
}

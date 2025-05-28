package io.cockroachdb.pestcontrol.api.cluster.status;

import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.model.Locality;
import io.cockroachdb.pestcontrol.model.Tier;

@Relation(value = LinkRelations.LOCALITY_REL,
        collectionRelation = LinkRelations.LOCALITIES_REL)
@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class LocalityModel extends RepresentationModel<LocalityModel> {
    @JsonProperty("tiers")
    private final List<Tier> tiers;

    public LocalityModel(List<Tier> tiers) {
        this.tiers = tiers;
    }

    public List<Tier> getTiers() {
        return tiers;
    }

    public String toTiers() {
        return Locality.toString(tiers);
    }
}

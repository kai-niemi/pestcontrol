package io.cockroachdb.pestcontrol.api.cluster.admin;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.cockroachdb.pestcontrol.api.LinkRelations;

@Relation(value = LinkRelations.CLUSTER_ADMIN_REL)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class AdminModel extends RepresentationModel<AdminModel> {
    public static AdminModel fromId(String clusterId) {
        return new AdminModel(clusterId);
    }

    private final String clusterId;

    public AdminModel(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

//    /**
//     * Find locality tiers up to a given sublevel.
//     *
//     * @param level the sublevel (1-based)
//     * @return list of tiers
//     */
//    public List<Locality> getLocalities(int level) {
//        Set<Locality> subLocalities = new LinkedHashSet<>();
//
//        localities.forEach(localityModel -> {
//            Locality subLocality = new Locality(new ArrayList<>(localityModel
//                    .getTiers()
//                    .stream()
//                    .limit(level)
//                    .toList()));
//
//            subLocalities.stream()
//                    .filter(x -> x.getTiers().equals(subLocality.getTiers()))
//                    .findFirst()
//                    .ifPresentOrElse(localityModel1 -> {
//                    }, () -> subLocalities.add(subLocality));
//        });
//
//        return new ArrayList<>(subLocalities);
//    }
}

package io.cockroachdb.pestcontrol.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.cockroachdb.pestcontrol.util.PatternUtils;
import io.cockroachdb.pestcontrol.util.TreeNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Locality implements Comparable<Locality> {
    /**
     * Round-robin pick host name/IPs mapped to localities.
     *
     * @param localities a map of localities to list of host names
     * @return collection of uniformly distributed host names from all localities
     */
    public static Collection<String> resolveJoinHosts(Map<Locality, List<String>> localities) {
        List<String> joinHosts = new ArrayList<>();

        TreeNode<Tier> root = TreeNode.of(Tier.of("cluster", ""));

        for (Locality locality : localities.keySet()) {
            TreeNode<Tier> next = root;
            for (Tier tier : locality.getTiers()) {
                next = next.addChild(tier);
                if (tier.getLevel() == 0) {
                    joinHosts.addAll(localities.get(locality)
                            .stream().limit(3).toList());
                }
            }
        }

        return joinHosts;
    }

    public static String toString(List<Tier> tiers) {
        List<String> tuples = new ArrayList<>();
        tiers.forEach(tier -> tuples.add(tier.getKey() + "=" + tier.getValue()));
        return String.join(",", tuples);
    }

    public static Locality fromTiers(String tiers) {
        List<Tier> tierList = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        PatternUtils.parseLocality(tiers)
                .forEach((k, v) -> {
                    Tier tier = new Tier();
                    tier.setLevel(i.getAndIncrement());
                    tier.setKey(k);
                    tier.setValue(v);

                    tierList.add(tier);
                });

        return new Locality(tierList);
    }

    private List<Tier> tiers;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    public Locality(List<Tier> tiers) {
        this.tiers = tiers;
    }

    public boolean matches(List<Tier> required) {
        return required
                       .stream()
                       .filter(tier -> tiers.contains(tier))
                       .count() == required.size();
    }

    public Optional<String> findRegionTier() {
        return getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst()
                .map(Tier::getValue);
    }

    @JsonProperty("tiers")
    public List<Tier> getTiers() {
        return tiers;
    }

    @JsonProperty("tiers")
    public void setTiers(List<Tier> tiers) {
        this.tiers = tiers;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @Override
    public int compareTo(Locality o) {
        return Locality.toString(tiers).compareToIgnoreCase(Locality.toString(o.getTiers()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Locality locality = (Locality) o;
        return Objects.equals(toString(), locality.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(toString());
    }

    @Override
    public String toString() {
        return Locality.toString(tiers);
    }
}

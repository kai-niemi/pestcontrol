package io.cockroachdb.pest.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.cockroachdb.pest.util.PatternUtils;
import io.cockroachdb.pest.util.TreeNode;

@Validated
public class Locality implements Comparable<Locality> {
    public static final String REGION = "region";

    public static final String CLUSTER = "cluster";

    /**
     * Round-robin pick host name/IPs mapped to localities.
     *
     * @param localities a map of localities to list of host names
     * @return collection of uniformly distributed host names from all localities
     */
    public static Collection<String> distributeJoinHosts(Map<Locality, List<String>> localities) {
        List<String> joinHosts = new ArrayList<>();

        TreeNode<Tier> root = TreeNode.of(Tier.of(CLUSTER, ""));

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

    private final List<Tier> tiers;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    public Locality(List<Tier> tiers) {
        this.tiers = tiers;
    }

    public boolean matches(List<Tier> required) {
        return required
                       .stream()
                       .filter(tiers::contains)
                       .count() == required.size();
    }

    public Optional<Tier> findRegionTier() {
        return getTiers().stream()
                .filter(tier -> tier.getKey().equals(REGION))
                .findFirst();
    }

    public List<Tier> getTiers() {
        return Collections.unmodifiableList(tiers);
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

    public static class Tier {
        public static Tier of(String k, String v) {
            return new Tier(k, v);
        }

        private String key;

        private String value;

        private Integer level;

        @JsonIgnore
        private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

        public Tier() {
        }

        public Tier(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Tier tier = (Tier) o;
            return Objects.equals(key, tier.key) && Objects.equals(value, tier.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}

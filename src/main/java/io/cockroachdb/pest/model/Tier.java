package io.cockroachdb.pest.model;

import java.util.Objects;

public class Tier {
    private String key;
    private String value;
    private Integer level;

    public Tier() {
    }

    public Tier(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static Tier of(String k, String v) {
        return new Tier(k, v);
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

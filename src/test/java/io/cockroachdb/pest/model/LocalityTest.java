package io.cockroachdb.pest.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit-test")
public class LocalityTest {
    @Test
    public void whenSingleZoneLocalities_expectEvenHostDistribution() {
        Map<Locality, List<String>> localities = new TreeMap<>();

        localities.put(Locality.fromTiers("az=eu-north-1a"),
                List.of("192.158.1.1:26257", "192.158.1.2:26258",
                        "192.158.1.3:26259", "192.158.1.4:26260",
                        "192.158.1.5:26261", "192.158.1.6:26262",
                        "192.158.1.22:26263", "192.158.1.23:26264"));

        Collection<String> joinHosts = Locality.distributeJoinHosts(localities);
        joinHosts.forEach(System.out::println);

        Assertions.assertEquals(3, joinHosts.size());
        Assertions.assertTrue(localities.get(Locality.fromTiers("az=eu-north-1a"))
                .stream().anyMatch(joinHosts::contains));
    }

    @Test
    public void whenSingleRegionEvenZoneLocalities_expectEvenHostDistribution() {
        Map<Locality, List<String>> localities = new TreeMap<>();

        localities.put(Locality.fromTiers("az=eu-north-1a"),
                List.of("192.158.1.1:26257", "192.158.1.2:26258",
                        "192.158.1.3:26259", "192.158.1.4:26257",
                        "192.158.1.5:26258", "192.158.1.6:26259"));
        localities.put(Locality.fromTiers("az=eu-north-1b"),
                List.of("192.158.1.7:26257", "192.158.1.8:26258",
                        "192.158.1.9:26259", "192.158.1.10:26257",
                        "192.158.1.11:26258", "192.158.1.12:26259"));

        Collection<String> joinHosts = Locality.distributeJoinHosts(localities);
        joinHosts.forEach(System.out::println);

        Assertions.assertEquals(2 * 3, joinHosts.size());
        Assertions.assertTrue(localities.get(Locality.fromTiers("az=eu-north-1a"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("az=eu-north-1b"))
                .stream().anyMatch(joinHosts::contains));
    }

    @Test
    public void whenSingleRegionLocalities_expectEvenHostDistribution() {
        Map<Locality, List<String>> localities = new TreeMap<>();

        localities.put(Locality.fromTiers("az=eu-north-1a"),
                List.of("192.158.1.1:26257", "192.158.1.2:26258",
                        "192.158.1.3:26259", "192.158.1.4:26257",
                        "192.158.1.5:26258", "192.158.1.6:26259"));
        localities.put(Locality.fromTiers("az=eu-north-1b"),
                List.of("192.158.1.7:26257", "192.158.1.8:26258",
                        "192.158.1.9:26259", "192.158.1.10:26257",
                        "192.158.1.11:26258", "192.158.1.12:26259"));
        localities.put(Locality.fromTiers("az=eu-north-1c"),
                List.of("192.158.1.13:26257", "192.158.1.14:26258",
                        "192.158.1.15:26259", "192.158.1.4:26257",
                        "192.158.1.16:26258", "192.158.1.17:26259"));

        Collection<String> joinHosts = Locality.distributeJoinHosts(localities);
        joinHosts.forEach(System.out::println);

        Assertions.assertEquals(3 * 3, joinHosts.size());
        Assertions.assertTrue(localities.get(Locality.fromTiers("az=eu-north-1a"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("az=eu-north-1b"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("az=eu-north-1c"))
                .stream().anyMatch(joinHosts::contains));
    }

    @Test
    public void whenSingleRegionZoneLocalities_expectEvenHostDistribution() {
        Map<Locality, List<String>> localities = new TreeMap<>();

        localities.put(Locality.fromTiers("region=eu-north-1,zone=eu-north-1a"),
                List.of("192.158.1.1:26257", "192.158.1.2:26258",
                        "192.158.1.3:26259", "192.158.1.4:26257",
                        "192.158.1.5:26258", "192.158.1.6:26259"));
        localities.put(Locality.fromTiers("region=eu-north-1,zone=eu-north-1b"),
                List.of("192.158.1.7:26257", "192.158.1.8:26258",
                        "192.158.1.9:26259", "192.158.1.10:26257",
                        "192.158.1.11:26258", "192.158.1.12:26259"));
        localities.put(Locality.fromTiers("region=eu-north-1,zone=eu-north-1c"),
                List.of("192.158.1.13:26257", "192.158.1.14:26258",
                        "192.158.1.15:26259", "192.158.1.4:26257",
                        "192.158.1.16:26258", "192.158.1.17:26259"));
        localities.put(Locality.fromTiers("region=eu-north-1,zone=eu-north-1d"),
                List.of("192.158.1.18:26257", "192.158.1.19:26258",
                        "192.158.1.20:26259", "192.158.1.21:26257",
                        "192.158.1.22:26258", "192.158.1.23:26259"));

        Collection<String> joinHosts = Locality.distributeJoinHosts(localities);
        joinHosts.forEach(System.out::println);
        Assertions.assertEquals(4 * 3, joinHosts.size());

        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-1,zone=eu-north-1a"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-1,zone=eu-north-1b"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-1,zone=eu-north-1c"))
                .stream().anyMatch(joinHosts::contains));
    }

    @Test
    public void whenMultiRegionLocalities_expectEvenHostDistribution() {
        Map<Locality, List<String>> localities = new TreeMap<>();

        localities.put(Locality.fromTiers("region=eu-north-1,zone=eu-north-1a"),
                List.of("192.158.1.1:26257", "192.158.1.2:26258",
                        "192.158.1.3:26259", "192.158.1.4:26257",
                        "192.158.1.5:26258", "192.158.1.6:26259"));
        localities.put(Locality.fromTiers("region=eu-north-2,zone=eu-north-2b"),
                List.of("192.158.1.7:26257", "192.158.1.8:26258",
                        "192.158.1.9:26259", "192.158.1.10:26257",
                        "192.158.1.11:26258", "192.158.1.12:26259"));
        localities.put(Locality.fromTiers("region=eu-north-3,zone=eu-north-3c"),
                List.of("192.158.1.13:26257", "192.158.1.14:26258",
                        "192.158.1.15:26259", "192.158.1.4:26257",
                        "192.158.1.16:26258", "192.158.1.17:26259"));
        localities.put(Locality.fromTiers("region=eu-north-4,zone=eu-north-4d"),
                List.of("192.158.1.18:26257", "192.158.1.19:26258",
                        "192.158.1.20:26259", "192.158.1.21:26257",
                        "192.158.1.22:26258", "192.158.1.23:26259"));

        Collection<String> joinHosts = Locality.distributeJoinHosts(localities);
        joinHosts.forEach(System.out::println);

        Assertions.assertEquals(4 * 3, joinHosts.size());
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-1,zone=eu-north-1a"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-2,zone=eu-north-2b"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-3,zone=eu-north-3c"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-4,zone=eu-north-4d"))
                .stream().anyMatch(joinHosts::contains));
    }

    @Test
    public void whenMultiRegionLocalities2_expectEvenHostDistribution() {
        Map<Locality, List<String>> localities = new TreeMap<>();

        localities.put(Locality.fromTiers("region=eu-north-1,zone=eu-north-1a"),
                List.of("192.158.1.1:26257"));
        localities.put(Locality.fromTiers("region=eu-north-2,zone=eu-north-2b"),
                List.of("192.158.1.7:26257"));
        localities.put(Locality.fromTiers("region=eu-north-3,zone=eu-north-3c"),
                List.of("192.158.1.13:26257"));
        localities.put(Locality.fromTiers("region=eu-north-4,zone=eu-north-4d"),
                List.of("192.158.1.18:26257"));

        Collection<String> joinHosts = Locality.distributeJoinHosts(localities);
        joinHosts.forEach(System.out::println);

        Assertions.assertEquals(4, joinHosts.size());
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-1,zone=eu-north-1a"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-2,zone=eu-north-2b"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-3,zone=eu-north-3c"))
                .stream().anyMatch(joinHosts::contains));
        Assertions.assertTrue(localities.get(Locality.fromTiers("region=eu-north-4,zone=eu-north-4d"))
                .stream().anyMatch(joinHosts::contains));
    }
}

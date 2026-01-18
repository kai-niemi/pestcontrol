package io.cockroachdb.pest.domain;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkAddressTest {
    @Test
    public void whenResolvingHostNames_expectSomething() {
        Assertions.assertNotNull(NetworkAddress.getCanonicalHostName());
        Assertions.assertNotNull(NetworkAddress.getExternalIP());
        Assertions.assertNotNull(NetworkAddress.getHostname());
        Assertions.assertNotNull(NetworkAddress.getLocalIP());
    }

    @Test
    public void whenAddressSubstitution_expectIncrementalPorts() {
        Assertions.assertEquals(":12345", NetworkAddress.from(":+12345").toAddressString());
        Assertions.assertEquals(":12345", NetworkAddress.from(":12345").toAddressString());
        Assertions.assertEquals("12345", NetworkAddress.from("12345").toAddressString());
        Assertions.assertEquals("1.2.3.4:12345", NetworkAddress.from("1.2.3.4:+12345").toAddressString());
        Assertions.assertEquals("1.2.3.4:12345", NetworkAddress.from("1.2.3.4:12345").toAddressString());
        Assertions.assertEquals("1.2.3.4", NetworkAddress.from("1.2.3.4").toAddressString());
        Assertions.assertThrows(NullPointerException.class, () -> NetworkAddress.from(null));

        Assertions.assertEquals("a:8080", NetworkAddress.from(":+8080", List.of("a","b", "c"), 0).toAddressString());
        Assertions.assertEquals("b:8081", NetworkAddress.from(":+8080", List.of("a","b", "c"), 1).toAddressString());
        Assertions.assertEquals("c:8082", NetworkAddress.from(":+8080", List.of("a","b", "c"), 2).toAddressString());
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> NetworkAddress.from(":+8080", List.of("a","b", "c"), 3));
    }
}

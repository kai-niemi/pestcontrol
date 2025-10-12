package io.cockroachdb.pest.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkingTest {
    @Test
    public void whenResolvingHostNames_expectSomething() {
        Assertions.assertNotNull(Networking.getCanonicalHostName());
        Assertions.assertNotNull(Networking.getExternalIP());
        Assertions.assertNotNull(Networking.getHostname());
        Assertions.assertNotNull(Networking.getLocalIP());
    }

    @Test
    public void whenOffsetPort_expectSomething() {
        Assertions.assertNull(Networking.offsetPort(null, 1));
        Assertions.assertEquals(":12346", Networking.offsetPort(":+12345", 1));
        Assertions.assertEquals("1.2.3.4:12346", Networking.offsetPort("1.2.3.4:+12345", 1));
        Assertions.assertEquals(":12345", Networking.offsetPort(":12345", 1));
        Assertions.assertEquals("1.2.3.4:12345", Networking.offsetPort("1.2.3.4:12345", 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Networking.offsetPort("12345", 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Networking.offsetPort("1.2.3.4", 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Networking.offsetPort("", 1));
    }
}

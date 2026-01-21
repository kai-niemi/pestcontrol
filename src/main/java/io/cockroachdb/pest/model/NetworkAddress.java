package io.cockroachdb.pest.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

public final class NetworkAddress implements Comparable<NetworkAddress> {
    private final String addressField;
    private final Integer portField;

    private NetworkAddress(String address) {
        String[] parts = address.split(":");
        if (parts.length == 2) {
            addressField = parts[0];
            portField = Integer.parseInt(parts[1]);
        } else {
            addressField = parts[0];
            portField = null;
        }
    }

    private NetworkAddress(String address, Integer port) {
        Objects.requireNonNull(address, "address is null");
        this.addressField = address;
        this.portField = port;
    }

    public static String resolve(String address) {
        if (!StringUtils.hasLength(address)) {
            return address;
        }
        return new PropertyPlaceholderHelper("${", "}").replacePlaceholders(address,
                placeholderName -> switch (placeholderName.toLowerCase()) {
                    case "localip", "local-ip" -> getLocalIP();
                    case "publicip", "public-ip", "externalip", "external-ip" -> getExternalIP();
                    case "hostname" -> getHostname();
                    default -> placeholderName;
                });
    }

    public static String getLocalIP() throws UncheckedIOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("checkip.amazonaws.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getExternalIP() throws UncheckedIOException {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return br.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getLoopbackHostname() {
        return InetAddress.getLoopbackAddress().getHostName();
    }

    public static String getHostname() throws UncheckedIOException {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getCanonicalHostName() throws UncheckedIOException {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static NetworkAddress from(String address) {
        return new NetworkAddress(address);
    }

    public static NetworkAddress from(String address, List<String> prefixes, int offset) {
        Objects.requireNonNull(address, "address is null");
        if (address.startsWith(":+") && !prefixes.isEmpty()) {
            address = "%s:%s".formatted(prefixes.get(offset), address.substring(1));
        }

        String addr = "";
        String port = "";

        if (address.startsWith(":")) {
            port = address.substring(1);
        } else {
            String[] parts = address.split(":");
            if (parts.length == 2) {
                addr = parts[0];
                port = parts[1];
            } else {
                addr = parts[0];
            }
        }

        if (port.startsWith("+")) {
            if (offset >= 0) {
                port = "" + (Integer.parseInt(port.substring(1)) + offset);
            } else {
                port = "" + Integer.parseInt(port.substring(1));
            }
        }

        return new NetworkAddress(addr, port.isEmpty() ? null : Integer.parseInt(port));
    }

    public Optional<String> getAddress() {
        return addressField.isEmpty() ? Optional.empty() : Optional.of(addressField);
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(portField);
    }

    public String toAddressString() {
        return "%s%s".formatted(addressField, portField == null ? "" : ":" + portField);
    }

    public NetworkAddress addPort(int port) {
        return new NetworkAddress(this.addressField, port);
    }

    @Override
    public String toString() {
        return toAddressString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetworkAddress that = (NetworkAddress) o;
        return Objects.equals(addressField, that.addressField) && Objects.equals(portField, that.portField);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(addressField);
        result = 31 * result + Objects.hashCode(portField);
        return result;
    }

    @Override
    public int compareTo(NetworkAddress o) {
        return this.toString().compareTo(o.toString());
    }
}

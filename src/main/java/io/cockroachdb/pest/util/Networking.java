package io.cockroachdb.pest.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

public abstract class Networking {
    private Networking() {
    }

    public static String resolve(String input) {
        if (!StringUtils.hasLength(input)) {
            return input;
        }
        return new PropertyPlaceholderHelper("${", "}")
                .replacePlaceholders(input,
                        placeholderName -> switch (placeholderName.toLowerCase()) {
                            case "localip", "local-ip" -> Networking.getLocalIP();
                            case "publicip", "public-ip", "externalip", "external-ip" -> Networking.getExternalIP();
                            case "hostname" -> Networking.getHostname();
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
}

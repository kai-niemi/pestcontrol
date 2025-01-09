package io.cockroachdb.pc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

public abstract class Networking {
    private Networking() {
    }

    public static String getLocalIP() throws UncheckedIOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("checkip.amazonaws.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getPublicIP() throws UncheckedIOException {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return br.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

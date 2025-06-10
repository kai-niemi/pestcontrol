package io.cockroachdb.pestcontrol.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IoUtils {
    private IoUtils() {
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        try (InputStream is = new BufferedInputStream(in)) {
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }
}

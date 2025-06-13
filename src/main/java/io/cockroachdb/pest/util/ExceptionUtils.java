package io.cockroachdb.pest.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String toString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}

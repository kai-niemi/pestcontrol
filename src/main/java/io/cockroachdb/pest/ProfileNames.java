package io.cockroachdb.pest;

public abstract class ProfileNames {
    public static final String DEV = "dev";
    public static final String WEB = "web";
    public static final String SECURE = "secure";
    public static final String VERBOSE = "verbose";
    public static final String VERBOSE_SSL = "verbose_ssl";
    public static final String VERBOSE_HTTP = "verbose_http";

    private ProfileNames() {
    }
}

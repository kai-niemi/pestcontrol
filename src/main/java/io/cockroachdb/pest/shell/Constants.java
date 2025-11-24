package io.cockroachdb.pest.shell;

public abstract class Constants {
    public static final String CONFIG_COMMANDS = "1) Configuration Commands";

    public static final String STATUS_COMMANDS = "2) Cluster Status Commands";

    public static final String NODE_COMMANDS = "3) Node Commands";

    public static final String CHAOS_COMMANDS = "4) Chaos API Commands";

    public static final String TOXIPROXY_COMMANDS = "5) Toxiproxy Commands";

    public static final String ADMIN_COMMANDS = "6) Admin Commands";

    private Constants() {
    }
}

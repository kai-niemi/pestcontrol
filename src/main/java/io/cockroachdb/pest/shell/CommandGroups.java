package io.cockroachdb.pest.shell;

public abstract class CommandGroups {
    public static final String CONFIG_COMMANDS = "1) Configuration Commands";

    public static final String CLUSTER_COMMANDS = "2) Cluster Commands";

//    public static final String STATUS_COMMANDS = "2) Cluster Status Commands";

    public static final String NODE_COMMANDS = "3) Node Commands";

    public static final String CHAOS_COMMANDS = "4) Chaos API Commands";

    public static final String HAPROXY_COMMANDS = "6) HAProxy Commands";

    public static final String TOXIPROXY_COMMANDS = "7) Toxiproxy Commands";

    public static final String ADMIN_COMMANDS = "8) Admin Commands";

    private CommandGroups() {
    }
}

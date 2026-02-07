package io.cockroachdb.pest.shell;

public abstract class CommandGroups {
    public static final String ADMIN_COMMANDS = "1) Admin Commands";

    public static final String CLUSTER_COMMANDS = "2) Cluster Commands";

    public static final String HAPROXY_COMMANDS = "3) HAProxy Commands";

    public static final String TOXIPROXY_COMMANDS = "4) Toxiproxy Commands";

    public static final String CHAOS_COMMANDS = "5) Chaos API Commands";

    public static final String NODE_COMMANDS = "6) Node Commands";

    private CommandGroups() {
    }
}

package io.cockroachdb.pest.web;

/**
 * Domain specific link relations with attached semantics,
 * in contrast to IANA standard links.
 */
public class LinkRelations {
    public static final String CANCEL_REL = "cancel";

    public static final String DELETE_REL = "delete";

    public static final String ENABLE_REL = "enable";

    public static final String DISABLE_REL = "disable";

    public static final String VERSION_REL = "version";

    public static final String CLUSTER_REL = "cluster";

    public static final String CLUSTER_TEMPLATE_REL = "cluster-template";

    public static final String CLUSTERS_REL = "clusters";

    public static final String OPERATOR_REL = "operator";

    public static final String OPERATOR_TEMPLATE_REL = "operator-template";

    public static final String NODES_REL = "nodes";

    public static final String CERTS_REL = "certs";

    public static final String NODE_START_REL = "node-start";

    public static final String NODE_STOP_REL = "node-stop";

    public static final String NODE_INIT_REL = "node-init";

    public static final String NODE_WIPE_REL = "node-wipe";

    public static final String NODE_GEN_HAPROXY_REL = "node-gen-haproxy";

    public static final String NODE_START_HAPROXY_REL = "node-start-haproxy";

    public static final String NODE_START_TOXIPROXY_REL = "node-start-toxiproxy";

    public static final String NODE_STOP_TOXIPROXY_REL = "node-stop-toxiproxy";

    public static final String NODE_STOP_HAPROXY_REL = "node-stop-haproxy";

    public static final String NODE_KILL_REL = "node-kill";

    public static final String NODE_INSTALL_REL = "node-install";

    public static final String DISRUPT_NODE_REL = "node-disrupt";

    public static final String RECOVER_NODE_REL = "node-recover";

    public static final String NODE_STATUS_REL = "node-status";

    public static final String NODE_DETAIL_REL = "node-detail";

    public static final String LOCALITY_DISRUPT_REL = "locality-disrupt";

    public static final String LOCALITY_RECOVER_REL = "locality-recover";

    public static final String CHARTS_REL = "charts";

    public static final String TOXIPROXY_REL = "toxiproxy";

    public static final String PROXIES_REL = "proxies";

    public static final String PROXY_REL = "proxy";

    public static final String TOXICS_REL = "toxics";

    public static final String TOXIC_REL = "toxic";

    public static final String RESET_REL = "reset";

    public static final String DATA_POINTS_REL = "data-points";

    public static final String ACTUATORS_REL = "actuators";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "pc";

    private LinkRelations() {
    }

}

package io.cockroachdb.pestcontrol.api;

public class LinkRelations {
    public static final String DATA_POINTS_REL = "data-points";

    public static final String ACTUATORS_REL = "actuators";

    public static final String CANCEL_REL = "cancel";

    public static final String DELETE_REL = "delete";

    public static final String ENABLE_REL = "enable";

    public static final String DISABLE_REL = "disable";

    public static final String VERSION_REL = "version";

    public static final String CLUSTER_REL = "cluster";

    public static final String CLUSTER_COLL_REL = "cluster-coll";

    public static final String CLUSTER_ADMIN_REL = "cluster-admin";

    public static final String CLUSTER_ADMIN_COLL_REL = "cluster-admin-coll";

    public static final String CLUSTER_STATUS_REL = "cluster-status";

    public static final String CLUSTER_STATUS_COLL_REL = "cluster-status-coll";

    public static final String CLUSTER_MACHINE_REL = "cluster-machine";

    public static final String CLUSTER_MACHINE_COLL_REL = "cluster-machine-coll";

    public static final String WORKLOAD_REL = "workload";

    public static final String WORKLOAD_COLL_REL = "workload-coll";

    public static final String START_REL = "start";

    public static final String STOP_REL = "stop";

    public static final String DISRUPT_REL = "disrupt";
    public static final String DISRUPT_LOC_REL = "disrupt-locality";

    public static final String RECOVER_REL = "recover";
    public static final String RECOVER_LOC_REL = "recover-locality";

    public static final String NODE_REL = "node";

    public static final String NODES_REL = "node-coll";

    public static final String NODE_STATUS_REL = "node-status";

    public static final String NODE_DETAIL_REL = "node-detail";

    public static final String TOXIPROXY_REL = "toxiproxy";

    public static final String CHARTS_REL = "charts";

    public static final String PROXY_REL = "proxy";

    public static final String PROXY_COLL_REL = "proxy-coll";

    public static final String FORM_REL = "form";

    public static final String TOXIC_REL = "toxic";

    public static final String TOXIC_COLL_REL = "toxic-coll";

    public static final String RESET_REL = "reset";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "pc";

    private LinkRelations() {
    }

}

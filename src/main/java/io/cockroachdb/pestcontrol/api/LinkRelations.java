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

    public static final String CLUSTERS_REL = "clusters";

    public static final String CLUSTER_STATUS_REL = "cluster-status";

    public static final String CLUSTER_STATUSES_REL = "cluster-statuses";

    public static final String CLUSTER_MACHINE_REL = "cluster-machine";

    public static final String CLUSTER_MACHINES_REL = "cluster-machines";

    public static final String DISRUPT_REL = "disrupt";

    public static final String RECOVER_REL = "recover";

    public static final String NODE_REL = "node";

    public static final String NODES_REL = "nodes";

    public static final String NODE_STATUS_REL = "node-status";

    public static final String NODE_DETAIL_REL = "node-detail";

    public static final String ADMIN_REL = "admin";

    public static final String TOXIPROXY_REL = "toxiproxy";

    public static final String CHARTS_REL = "charts";

    public static final String PROXY_REL = "proxy";

    public static final String PROXIES_REL = "proxies";

    public static final String FORM_REL = "form";

    public static final String TOXIC_REL = "toxic";

    public static final String TOXICS_REL = "toxics";

    public static final String RESET_REL = "reset";

    public static final String WORKLOAD_REL = "workload";

    public static final String WORKLOADS_REL = "workloads";

    public static final String LOCALITY_REL = "locality";

    public static final String LOCALITIES_REL = "localities";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "pc";

    private LinkRelations() {
    }

}

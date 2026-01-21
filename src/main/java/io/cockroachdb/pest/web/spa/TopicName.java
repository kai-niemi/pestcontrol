package io.cockroachdb.pest.web.spa;

public enum TopicName {
    DASHBOARD_NODE_STATUS("/topic/dashboard/status"),
    DASHBOARD_REFRESH_PAGE("/topic/dashboard/refresh"),
    DASHBOARD_TOAST_MESSAGE("/topic/dashboard/toast"),
    DASHBOARD_MODEL_UPDATE("/topic/dashboard/update"),
    METRIC_CHARTS_UPDATE("/topic/metric/charts"),
    METRIC_REFRESH_PAGE("/topic/metric/refresh");

    final String value;

    TopicName(java.lang.String value) {
        this.value = value;
    }
}

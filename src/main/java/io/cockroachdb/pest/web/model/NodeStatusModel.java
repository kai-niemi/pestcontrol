package io.cockroachdb.pest.web.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.hateoas.EntityModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.cockroachdb.pest.model.status.NodeStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeStatusModel extends EntityModel<NodeStatus> {
    public NodeStatusModel(NodeStatus nodeStatus) {
        super(nodeStatus);
    }

    public Integer getNodeId() {
        return getContent().getDesc().getNodeId();
    }

    public String getSqlAddress() {
        return getContent().getDesc().getSqlAddress().getAddressField();
    }

    public String getBuildTag() {
        return getContent().getDesc().getBuildTag();
    }

    public String getLastActive() {
        long t = Long.parseLong(getContent().getUpdatedAt());
        Timestamp ts = Timestamp.from(Instant.ofEpochMilli(TimeUnit.NANOSECONDS.toMillis(t)));
        return ts.toString();
    }

    public String getLocality() {
        List<String> tuples = new ArrayList<>();
        getContent().getDesc().getLocality().getTiers()
                .forEach(tier -> tuples.add(tier.getKey() + "=" + tier.getValue()));
        return String.join(",", tuples);
    }

    public boolean isAvailable() {
        return getContent().getDesc().getNodeId() > 0;
    }

    @JsonIgnore
    public String getCardClass() {
        if (isAvailable()) {
            return "border-success alert-success";
        }
        return "border-warning alert-warning";
    }

    @JsonIgnore
    public String getCardImage() {
        if (isAvailable()) {
            return "#db-ok";
        }
        return "#db-warn";
    }
}

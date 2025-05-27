package io.cockroachdb.pestcontrol.operator;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.api.LinkRelations;
import io.cockroachdb.pestcontrol.api.machine.MachinesForm;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.MachineProperties;
import io.cockroachdb.pestcontrol.shell.support.HypermediaClient;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.curied;

@Component
public class RemoteClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.remote_insecure, ClusterType.remote_secure)
                .contains(clusterType);
    }

    @Override
    public void startNode(ClusterProperties clusterProperties, Integer nodeId) {
        MachinesForm form = new MachinesForm();
        form.setNodeId(nodeId);
        form.setMachines(clusterProperties.getMachines());

        MachineProperties machineProperties = clusterProperties.getNodeById(nodeId);

        ResponseEntity<MachinesForm> response = hypermediaClient.post(
                hypermediaClient.from(machineProperties.getBaseUrl())
                        .follow(curied(LinkRelations.CURIE_NAMESPACE, LinkRelations.NODES_REL).value())
                        .asLink(),
                form,
                MachinesForm.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("HTTP status: {}", response);
        } else {
            logger.warn("Unexpected HTTP status: {}", response);
        }
    }

    @Override
    public void init(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void killNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disruptNodes(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recoverNodes(ClusterProperties cluster, String locality) {
        throw new UnsupportedOperationException();
    }
}

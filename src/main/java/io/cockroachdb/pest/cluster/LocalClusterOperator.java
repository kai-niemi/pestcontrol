package io.cockroachdb.pest.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.model.ApplicationProperties;
import io.cockroachdb.pest.model.ClusterProperties;
import io.cockroachdb.pest.model.ClusterType;
import io.cockroachdb.pest.model.Locality;
import io.cockroachdb.pest.model.NodeProperties;
import static io.cockroachdb.pest.util.ProcessUtils.executeCommand;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public boolean supports(ClusterType clusterType) {
        return false;
    }

    @Override
    public String install(ClusterProperties clusterProperties, Integer nodeId) {
        List<String> args = List.of("./cluster-admin", "agent-install",
                "--version=" + clusterProperties.getVersion()
        );
        return executeCommand(applicationProperties.getScriptDirectory(), args);
    }

    @Override
    public String startNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        Map<Locality, List<String>> hosts = new TreeMap<>();

        clusterProperties.getNodes()
                .forEach(np -> {
                    Locality locality = Locality.fromTiers(np.getLocality());
                    hosts.computeIfAbsent(locality, x -> new ArrayList<>())
                            .add(np.getListenAddr());
                });

        Collection<String> joinHosts = Locality.distributeJoinHosts(hosts);

        List<String> args = List.of("./cluster-admin", "agent-start",
                "--name=n" + nodeId,
                "--locality=" + nodeProperties.getLocality(),
                "--listen-addr=" + nodeProperties.getListenAddr(),
                "--advertise-addr=" + nodeProperties.getAdvertiseAddr(),
                "--sql-addr=" + nodeProperties.getSqlAddr(),
                "--http-addr=" + nodeProperties.getHttpAddr(),
                "--join=" + String.join(",", joinHosts)
        );

        return executeCommand(applicationProperties.getScriptDirectory(),args);
    }

    @Override
    public String stopNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "agent-stop",
                "--sql-addr=" + nodeProperties.getSqlAddr()
        );

        return executeCommand(applicationProperties.getScriptDirectory(),args);
    }

    @Override
    public String init(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "agent-init",
                "--listen-addr=" + nodeProperties.getListenAddr(),
                "--sql-addr=" + nodeProperties.getSqlAddr()
        );

        return executeCommand(applicationProperties.getScriptDirectory(),args);
    }

    @Override
    public String killNode(ClusterProperties clusterProperties, Integer nodeId) {
        return stopNode(clusterProperties, nodeId);
    }

    @Override
    public String disruptNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "disrupt", nodeProperties.getSqlAddr());

        return executeCommand(applicationProperties.getScriptDirectory(),args);
    }

    @Override
    public String recoverNode(ClusterProperties clusterProperties, Integer nodeId) {
        NodeProperties nodeProperties = clusterProperties.findNodePropertiesById(nodeId);

        List<String> args = List.of("./cluster-admin", "recover", nodeProperties.getSqlAddr());

        return executeCommand(applicationProperties.getScriptDirectory(),args);
    }

    @Override
    public String disruptNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recoverNodes(ClusterProperties clusterProperties, String locality) {
        throw new UnsupportedOperationException();
    }

}

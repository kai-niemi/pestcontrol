package io.cockroachdb.pest.shell;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.domain.Cluster;

@Component
public class HAProxyCommands extends AbstractShellCommand {
    private static final String NODE_ID_OPTION = "The node ID, ID range (1-N) or 'all' to include all nodes";

    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Command(description = "Generate haproxy.cfg",
            help = "Generate haproxy.cfg on specified host(s)",
            name = {"haproxy", "gen"},
            group = CommandGroups.HAPROXY_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void genHAProxy(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id)  throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : nodeIdRange(id)) {
            clusterOperator.proxyOperator(cluster).genHAProxyCfg(x);
        }
    }

    @Command(description = "Start HAProxy server",
            help = "Start HAProxy on specified host(s)",
            name = {"haproxy", "start"},
            group = CommandGroups.HAPROXY_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void startHaProxy(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id)  throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : nodeIdRange(id)) {
            clusterOperator.proxyOperator(cluster).startHAProxy(x);
        }
    }

    @Command(description = "Stop HAProxy server",
            help = "Stop HAProxy on specified host(s)",
            name = {"haproxy", "stop"},
            group = CommandGroups.HAPROXY_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void stopHAProxy(
            @Option(description = NODE_ID_OPTION, defaultValue = "1",
                    shortName = 'n', longName = "nodeId") String id) throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : nodeIdRange(id)) {
            clusterOperator.proxyOperator(cluster).stopHAProxy(x);
        }
    }
}

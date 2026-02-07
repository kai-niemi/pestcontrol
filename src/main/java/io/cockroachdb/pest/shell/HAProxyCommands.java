package io.cockroachdb.pest.shell;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.ClusterOperatorProvider;
import io.cockroachdb.pest.model.Cluster;

@Component
public class HAProxyCommands extends AbstractShellCommand {
    @Autowired
    private ClusterOperatorProvider clusterOperatorProvider;

    @Command(description = "Generate haproxy.cfg",
            help = "Generate haproxy.cfg on specified host(s)",
            name = {"generate", "haproxy", "config"},
            group = CommandGroups.HAPROXY_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            completionProvider = "nodeRangeProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void genHAProxy(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : nodeIdRange(id)) {
            clusterOperator.proxyOperator(cluster).genHAProxyCfg(x);
        }
    }

    @Command(description = "Start HAProxy server",
            help = "Start HAProxy on specified host(s)",
            name = {"start","haproxy"},
            group = CommandGroups.HAPROXY_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            completionProvider = "nodeRangeProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void startHaProxy(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : nodeIdRange(id)) {
            clusterOperator.proxyOperator(cluster).startHAProxy(x);
        }
    }

    @Command(description = "Stop HAProxy server",
            help = "Stop HAProxy on specified host(s)",
            name = {"stop","haproxy"},
            group = CommandGroups.HAPROXY_COMMANDS,
            availabilityProvider = "ifHostedCluster",
            completionProvider = "nodeRangeProvider",
            exitStatusExceptionMapper = "commandExceptionMapper")
    public void stopHAProxy(
            @Argument(description = NODE_ID_OPTION, defaultValue = "1", index = 0) String id) throws IOException {
        Cluster cluster = selectedCluster();
        ClusterOperator clusterOperator = clusterOperatorProvider.clusterOperator(cluster.getClusterId());
        for (Integer x : nodeIdRange(id)) {
            clusterOperator.proxyOperator(cluster).stopHAProxy(x);
        }
    }
}

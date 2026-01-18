package io.cockroachdb.pest.cluster.local;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.cockroachdb.pest.cluster.ClusterOperator;
import io.cockroachdb.pest.cluster.DisruptionOperator;
import io.cockroachdb.pest.cluster.NodeOperator;
import io.cockroachdb.pest.cluster.ProxyOperator;
import io.cockroachdb.pest.cluster.StatusOperator;
import io.cockroachdb.pest.cluster.repository.MetaDataRepository;
import io.cockroachdb.pest.config.RestClientProvider;
import io.cockroachdb.pest.domain.ApplicationProperties;
import io.cockroachdb.pest.domain.Cluster;
import io.cockroachdb.pest.domain.ClusterType;

@Component
public class LocalClusterOperator implements ClusterOperator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestClientProvider restClientProvider;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public StatusOperator statusOperator(Cluster cluster) {
        RestClient restClient = restClientProvider.apply(cluster.getClusterType());
        return new LocalStatusOperator(cluster, restClient, metaDataRepository);
    }

    @Override
    public DisruptionOperator disruptionOperator(Cluster cluster) {
        return new DisruptionOperator() {
            @Override
            public String disruptNode(Integer nodeId) throws IOException {
                return nodeOperator(cluster).killNode(nodeId);
            }

            @Override
            public String recoverNode(Integer nodeId) throws IOException {
                return nodeOperator(cluster).startNode(nodeId);
            }

            @Override
            public String disruptLocality(String locality) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String recoverLocality(String locality) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NodeOperator nodeOperator(Cluster cluster) {

        final LocalNodeOperator localNodeOperator
                = new LocalNodeOperator(cluster, applicationProperties);
        return new NodeOperator() {
            @Override
            public String certs(List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles)
            throws IOException {
                return localNodeOperator.certs(nodeIds, keyFiles);
            }

            @Override
            public String install(Integer nodeId) throws IOException {
                return localNodeOperator.install(nodeId);
            }

            @Override
            public String init(Integer nodeId) throws IOException {
                return localNodeOperator.init(nodeId);
            }

            @Override
            public String wipe(Integer nodeId, boolean all) throws IOException {
                return localNodeOperator.wipe(nodeId, all);
            }

            @Override
            public String startNode(Integer nodeId) throws IOException {
                startProxy(nodeId);
                return localNodeOperator.startNode(nodeId);
            }

            @Override
            public String stopNode(Integer nodeId) throws IOException {
                deleteProxy(nodeId);
                return localNodeOperator.stopNode(nodeId);
            }

            @Override
            public String killNode(Integer nodeId) throws IOException {
                deleteProxy(nodeId);
                return localNodeOperator.killNode(nodeId);
            }

            @Override
            public String sqlNode(Integer nodeId) throws IOException {
                return localNodeOperator.sqlNode(nodeId);
            }

            @Override
            public String statusNode(Integer nodeId)throws IOException  {
                return localNodeOperator.statusNode(nodeId);
            }

            private void startProxy(Integer nodeId) throws IOException {
                if (isToxiProxyAvailable()) {
                    Cluster.Node node = cluster.getNodeById(nodeId);
                    applicationProperties.createToxiProxyClient()
                            .createProxy(node.getName(),
                                    Objects.requireNonNull(node.getAdvertiseProxyAddr()),
                                    Objects.requireNonNull(node.getListenAddr()));
                }
            }

            private void deleteProxy(Integer nodeId) throws IOException {
                if (isToxiProxyAvailable()) {
                    Cluster.Node node = cluster.getNodeById(nodeId);
                    applicationProperties
                            .createToxiProxyClient()
                            .getProxies()
                            .stream()
                            .filter(x -> node.getName().equals(x.getName()))
                            .findFirst()
                            .ifPresent(proxy -> {
                                try {
                                    proxy.delete();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }

            private boolean isToxiProxyAvailable() {
                try {
                    applicationProperties.createToxiProxyClient().version();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        };
    }

    @Override
    public ProxyOperator proxyOperator(Cluster cluster) {
        return new LocalProxyOperator(cluster, applicationProperties.getDirectories());
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure)
                .contains(clusterType);
    }

}

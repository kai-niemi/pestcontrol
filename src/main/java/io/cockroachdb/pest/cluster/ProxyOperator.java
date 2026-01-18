package io.cockroachdb.pest.cluster;

import java.io.IOException;

public interface ProxyOperator {
    String genHAProxyCfg(Integer nodeId) throws IOException;

    String startHAProxy(Integer nodeId) throws IOException;

    String stopHAProxy(Integer nodeId) throws IOException;
}

package io.cockroachdb.pest.cluster;

import java.io.IOException;

public interface DisruptionOperator {
    String disruptNode(Integer nodeId) throws IOException;

    String recoverNode(Integer nodeId) throws IOException;

    String disruptLocality(String locality) throws IOException;

    String recoverLocality(String locality) throws IOException;
}

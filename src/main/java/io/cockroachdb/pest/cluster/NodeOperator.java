package io.cockroachdb.pest.cluster;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface NodeOperator {
    String certs(List<Integer> nodeIds, Map<Integer, List<Path>> keyFiles) throws IOException;

    String install(Integer nodeId, String version) throws IOException;

    String init(Integer nodeId) throws IOException;

    String wipe(Integer nodeId, boolean all) throws IOException;

    String startNode(Integer nodeId) throws IOException;

    String stopNode(Integer nodeId) throws IOException;

    String killNode(Integer nodeId) throws IOException;

    String sqlNode(Integer nodeId) throws IOException;

    String statusNode(Integer nodeId) throws IOException;
}

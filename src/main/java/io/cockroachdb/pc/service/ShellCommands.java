package io.cockroachdb.pc.service;

import java.util.List;

import io.cockroachdb.pc.schema.NodeModel;

public interface ShellCommands {
    default List<String> disrupt(NodeModel nodeModel) {
        return List.of("./cluster-admin", "disrupt", nodeModel.getNodeDetail().getSqlAddressPort());
    }

    default List<String> recover(NodeModel nodeModel) {
        return List.of("./cluster-admin", "recover", nodeModel.getNodeDetail().getSqlAddressPort());
    }
}

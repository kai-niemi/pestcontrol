package io.cockroachdb.pestcontrol.operator;

import java.util.List;

import io.cockroachdb.pestcontrol.model.NodeProperties;

public interface ShellCommands {
    default List<String> start(NodeProperties nodeProperties) {
        return List.of("./cluster-admin", "disrupt", nodeProperties.getSqlAddr());
    }

    default List<String> disrupt(NodeProperties nodeProperties) {
        return List.of("./cluster-admin", "disrupt", nodeProperties.getSqlAddr());
    }

    default List<String> recover(NodeProperties nodeProperties) {
        return List.of("./cluster-admin", "recover", nodeProperties.getSqlAddr());
    }
}

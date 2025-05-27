package io.cockroachdb.pestcontrol.operator;

import java.util.List;

import io.cockroachdb.pestcontrol.model.MachineProperties;

public interface ShellCommands {
    default List<String> start(MachineProperties machineProperties) {
        return List.of("./cluster-admin", "disrupt", machineProperties.getSqlAddr());
    }

    default List<String> disrupt(MachineProperties machineProperties) {
        return List.of("./cluster-admin", "disrupt", machineProperties.getSqlAddr());
    }

    default List<String> recover(MachineProperties machineProperties) {
        return List.of("./cluster-admin", "recover", machineProperties.getSqlAddr());
    }
}

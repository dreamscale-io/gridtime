package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.WatcherType;

import java.util.List;

public interface NotifySeeTrigger {

    void notifyOnSee(WatcherType watcherType, Object objectSeen);
}

package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.Torchie;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public interface LiveQueue {

    void submit(UUID workerId, Worker<TileInstructions> worker);

}

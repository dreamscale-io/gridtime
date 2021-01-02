package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

import java.util.List;

public interface TransactionWrappable {

   void runInTransaction() throws InterruptedException;

}

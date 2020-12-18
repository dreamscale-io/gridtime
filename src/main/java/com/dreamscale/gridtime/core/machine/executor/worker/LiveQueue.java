package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.Torchie;

public interface LiveQueue {

    void submitToLiveQueue(Torchie torchie);

}

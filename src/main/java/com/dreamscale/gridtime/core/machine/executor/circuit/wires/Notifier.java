package com.dreamscale.gridtime.core.machine.executor.circuit.wires;

import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyDoneTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyFailureTrigger;


public interface Notifier {

    void notifyOnDone(NotifyDoneTrigger notifyTrigger);

    void notifyOnFail(NotifyFailureTrigger notifyTrigger);
}

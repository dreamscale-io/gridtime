package com.dreamscale.gridtime.core.machine.executor.worker;

public interface Worker<T> {

    T whatsNext();

    boolean isWorkerReady();
}

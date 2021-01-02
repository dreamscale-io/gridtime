package com.dreamscale.gridtime.core.machine.executor.circuit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionWrapper {

    @Transactional
    public void wrapWithTransaction(TransactionWrappable instruction) throws InterruptedException {
        instruction.runInTransaction();
    }
}

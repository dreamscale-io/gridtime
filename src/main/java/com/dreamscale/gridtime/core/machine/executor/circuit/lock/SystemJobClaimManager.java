package com.dreamscale.gridtime.core.machine.executor.circuit.lock;

import com.dreamscale.gridtime.core.domain.work.LockRepository;
import com.dreamscale.gridtime.core.exception.UnableToLockException;
import com.dreamscale.gridtime.core.machine.executor.worker.WorkerClaim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class SystemJobClaimManager {


    @Autowired
    LockRepository lockRepository;




    public WorkerClaim claimIfNotRunning(UUID workerId, Object jobClaim) {
        return null;
    }

    public void finishClaim(WorkerClaim workerClaim) {

    }

    public void cleanUpStaleClaims(LocalDateTime now) {
        // TODO if something fails, and ends up stuck and in progress state, we need to be able to cancel out the DB job and recover
    }
}

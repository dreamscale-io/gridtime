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

    private static final Long WORKER_EXCLUSIVE_WRITE_LOCK = Long.MAX_VALUE;
    private static final Long TORCHIE_EXCLUSIVE_WRITE_LOCK = Long.MAX_VALUE - 1;

    private static final Long SYSTEM_JOB_LOCK = Long.MAX_VALUE - 2;

    @Autowired
    LockRepository lockRepository;


    private void releaseWorkerLock(Long lockNumber) {
        boolean release = lockRepository.releaseLock(lockNumber);
    }

    private void tryToAcquireWorkerLock(Long lockNumber) {

        boolean lockAcquired = false;
        int tries = 0;

        while (!lockAcquired && tries < 10) {
            lockAcquired = lockRepository.tryToAcquireLock(lockNumber);
            tries++;
        }

        if (!lockAcquired) {
            throw new UnableToLockException("Unable to acquire worker lock after 10 tries");
        }
    }


    public void tryToAcquireTorchieExclusiveLock() {
        tryToAcquireWorkerLock(TORCHIE_EXCLUSIVE_WRITE_LOCK);
    }

    public void releaseTorchieExclusiveLock() {
        releaseWorkerLock(TORCHIE_EXCLUSIVE_WRITE_LOCK);
    }

    public void tryToAcquireWorkerExclusiveLock() {
        tryToAcquireWorkerLock(WORKER_EXCLUSIVE_WRITE_LOCK);
    }

    public void releaseWorkerExclusiveLock() {
        releaseWorkerLock(WORKER_EXCLUSIVE_WRITE_LOCK);
    }

    public void tryToAcquireSystemJobLock() {
        tryToAcquireWorkerLock(SYSTEM_JOB_LOCK);
    }

    public void releaseSystemJobLock() {
        releaseWorkerLock(WORKER_EXCLUSIVE_WRITE_LOCK);
    }

    public WorkerClaim claimIfNotRunning(UUID workerId, Object jobClaim) {
        return null;
    }

    public void finishClaim(WorkerClaim workerClaim) {

    }

    public void cleanUpStaleClaims(LocalDateTime now) {
        // TODO if something fails, and ends up stuck and in progress state, we need to be able to cancel out the DB job and recover
    }
}

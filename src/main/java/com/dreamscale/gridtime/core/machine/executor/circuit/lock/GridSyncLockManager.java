package com.dreamscale.gridtime.core.machine.executor.circuit.lock;

import com.dreamscale.gridtime.core.domain.work.LockRepository;
import com.dreamscale.gridtime.core.exception.UnableToLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GridSyncLockManager {

    private static final Long PLEXER_SYNC_WRITE_LOCK = Long.MAX_VALUE;
    private static final Long TORCHIE_SYNC_WRITE_LOCK = Long.MAX_VALUE - 1;

    private static final Long SYSTEM_JOB_SYNC_LOCK = Long.MAX_VALUE - 2;

    @Autowired
    LockRepository lockRepository;


    private void releaseSyncLock(Long lockNumber) {
        boolean release = lockRepository.releaseLock(lockNumber);
    }

    private void tryToAcquireSyncLock(Long lockNumber) {

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


    public void tryToAcquireTorchieSyncLock() {
        tryToAcquireSyncLock(TORCHIE_SYNC_WRITE_LOCK);
    }

    public void releaseTorchieSyncLock() {
        releaseSyncLock(TORCHIE_SYNC_WRITE_LOCK);
    }

    public void tryToAcquirePlexerSyncLock() {
        tryToAcquireSyncLock(PLEXER_SYNC_WRITE_LOCK);
    }

    public void releasePlexerSyncLock() {
        releaseSyncLock(PLEXER_SYNC_WRITE_LOCK);
    }

    public void tryToAcquireSystemJobSyncLock() {
        tryToAcquireSyncLock(SYSTEM_JOB_SYNC_LOCK);
    }

    public void releaseSystemJobLock() {
        releaseSyncLock(PLEXER_SYNC_WRITE_LOCK);
    }
}

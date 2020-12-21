package com.dreamscale.gridtime.core.machine.executor.circuit.lock;

import com.dreamscale.gridtime.core.domain.work.LockRepository;
import com.dreamscale.gridtime.core.exception.UnableToLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GridSyncLockManager {

    private static final Long PLEXER_SYNC_WRITE_LOCK = 1L;
    private static final Long TORCHIE_SYNC_WRITE_LOCK = 2L;

    private static final Long SYSTEM_JOB_SYNC_LOCK = 3L;

    @Autowired
    LockRepository lockRepository;

    private void releaseSyncLock(Long lockNumber) {
        boolean success = lockRepository.releaseLock(lockNumber);

        if (!success) {
            log.error("Unable to release lock "+lockNumber);
        }

        int tries = 0;
        try {
            while (!success && tries < 10) {
                success = lockRepository.releaseLock(lockNumber);

                if (success) {
                    break;
                } else {
                    log.debug("Lock release failed, retrying");
                    Thread.sleep(100);
                }
                tries++;
            }
        } catch (InterruptedException ex) {
            log.error("Interrupted", ex);
         }

    }


    private void tryToAcquireSyncLock(Long lockNumber) {

        boolean lockAcquired = false;
        int tries = 0;

        try {
            while (!lockAcquired && tries < 10) {
                lockAcquired = lockRepository.tryToAcquireLock(lockNumber);

                if (lockAcquired) {
                    break;
                } else {
                    log.debug("Lock attempt failed, retrying");
                    Thread.sleep(100);
                }
                tries++;
            }
        } catch (InterruptedException ex) {
            log.error("Interrupted", ex);
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

    public void releaseSystemJobSyncLock() {
        releaseSyncLock(SYSTEM_JOB_SYNC_LOCK);
    }
}

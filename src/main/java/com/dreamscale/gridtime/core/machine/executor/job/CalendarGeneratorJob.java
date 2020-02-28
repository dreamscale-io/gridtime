package com.dreamscale.gridtime.core.machine.executor.job;

import com.dreamscale.gridtime.core.machine.executor.circuit.lock.GridSyncLockManager;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CalendarGeneratorJob implements JobCapability {

    @Autowired
    GridSyncLockManager gridSyncLockManager;

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    FeatureCacheManager featureCacheManager;

    @Override
    public void start() {

        //everyday, I'm going to generate enough tiles for stay 1 week ahead


        //when I wake up, get the lock for my job, and the configuration for my job,
        //configure myself.

        //if the lock is already taken, go into a job state, of JOB_RUNNING_ON_ANOTHER_SERVER

        //get the bookmark position for where my job left off processing.

    }

    @Override
    public void destroy() {

    }

    public boolean hasWorkToDo(LocalDateTime now) {
        return false;
    }

    public Program createStayAheadProgram(LocalDateTime now) {
        return null;
    }

    public Object getJobType() {
        return null;
    }

    public Object createJobClaim(LocalDateTime now) {
        return null;
    }
}

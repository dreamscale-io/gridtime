package com.dreamscale.gridtime.core.machine.executor.circuit.lock;

import com.dreamscale.gridtime.core.domain.job.GridtimeSystemJobClaimEntity;
import com.dreamscale.gridtime.core.domain.job.GridtimeSystemJobClaimRepository;
import com.dreamscale.gridtime.core.domain.job.JobStatusType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.executor.job.SystemJobDescriptor;
import com.dreamscale.gridtime.core.machine.executor.worker.SystemJobClaim;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class SystemExclusiveJobClaimManager {

    @Autowired
    GridtimeSystemJobClaimRepository gridtimeSystemJobClaimRepository;

    @Autowired
    GridClock gridClock;

    private static final int MINUTES_BEFORE_EXPIRING = 20;

    public SystemJobClaim claimIfNotRunning(LocalDateTime now, UUID workerId, SystemJobDescriptor jobDescriptor) {

        GridtimeSystemJobClaimEntity existingClaim = gridtimeSystemJobClaimRepository.findInProgressJobsByJobType(jobDescriptor.getJobType().name());

        SystemJobClaim newJobClaim = null;

        if (existingClaim != null) {

            if (existingClaim.getLastHeartbeat().isBefore(now.minusMinutes(MINUTES_BEFORE_EXPIRING))) {

                existingClaim.setJobStatus(JobStatusType.ABORTED);
                existingClaim.setFinishedOn(now);
                existingClaim.setErrorMessage("Aborting stale job claim");

                gridtimeSystemJobClaimRepository.save(existingClaim);

                log.warn("Aborting {} Job started on {} still not completed,",
                        existingClaim.getJobType().name(), existingClaim.getStartedOn());

                existingClaim = null;

            } else {
                log.warn("Existing job {} already running, unable to claim", existingClaim.getJobType().name());
            }
        }

        if (existingClaim == null) {
            GridtimeSystemJobClaimEntity jobClaimEntity = new GridtimeSystemJobClaimEntity();
            jobClaimEntity.setId(UUID.randomUUID());
            jobClaimEntity.setJobType(jobDescriptor.getJobType());
            jobClaimEntity.setJobDescriptorJson(JSONTransformer.toJson(jobDescriptor));
            jobClaimEntity.setStartedOn(now);
            jobClaimEntity.setLastHeartbeat(now);
            jobClaimEntity.setJobStatus(JobStatusType.IN_PROGRESS);
            jobClaimEntity.setClaimingWorkerId(workerId);

            gridtimeSystemJobClaimRepository.save(jobClaimEntity);

            newJobClaim = new SystemJobClaim(jobClaimEntity.getId(), workerId, jobClaimEntity.getJobType());

        }

        return newJobClaim;
    }

    public void finishClaim(SystemJobClaim systemJobClaim) {

        LocalDateTime now = gridClock.now();

        GridtimeSystemJobClaimEntity existingClaim = gridtimeSystemJobClaimRepository.findById(systemJobClaim.getJobClaimId());

        if (existingClaim != null) {
            log.debug("Found existing claim");
            existingClaim.setFinishedOn(now);
            existingClaim.setJobStatus(JobStatusType.DONE);

            gridtimeSystemJobClaimRepository.save(existingClaim);
        }

    }

    public void failClaim(SystemJobClaim systemJobClaim, String errorMessage) {

        LocalDateTime now = gridClock.now();

        GridtimeSystemJobClaimEntity existingClaim = gridtimeSystemJobClaimRepository.findById(systemJobClaim.getJobClaimId());

        if (existingClaim != null) {
            existingClaim.setFinishedOn(now);
            existingClaim.setJobStatus(JobStatusType.FAILED);
            existingClaim.setErrorMessage(errorMessage);

            gridtimeSystemJobClaimRepository.save(existingClaim);
        }

    }

    public void abortClaim(SystemJobClaim systemJobClaim) {
        LocalDateTime now = gridClock.now();

        GridtimeSystemJobClaimEntity existingClaim = gridtimeSystemJobClaimRepository.findById(systemJobClaim.getJobClaimId());

        if (existingClaim != null) {
            existingClaim.setFinishedOn(now);
            existingClaim.setJobStatus(JobStatusType.ABORTED);

            gridtimeSystemJobClaimRepository.save(existingClaim);
        }
    }

    public void updateHeartbeat(LocalDateTime now, UUID workerId) {
        GridtimeSystemJobClaimEntity existingClaim = gridtimeSystemJobClaimRepository.findInProgressJobByClaimingWorkerId(workerId);

        if (existingClaim != null) {
            existingClaim.setLastHeartbeat(now);
           gridtimeSystemJobClaimRepository.save(existingClaim);
        }
    }
}

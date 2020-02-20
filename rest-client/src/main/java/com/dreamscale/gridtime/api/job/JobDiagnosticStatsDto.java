package com.dreamscale.gridtime.api.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobDiagnosticStatsDto {

    UUID organizationId;

    UUID jobId;
    String jobName;
    String jobType;

    LocalDateTime startedOn;
    LocalDateTime lastHeartbeat;

    String lastExitStatus;
    String runStatus;

    UUID claimingWorkerId;

}

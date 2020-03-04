package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.domain.job.SystemJobType;
import com.dreamscale.gridtime.core.machine.executor.job.SystemJobDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SystemJobClaim {

    private UUID jobClaimId;

    private UUID workerId;

    private SystemJobType jobType;

}

package com.dreamscale.gridtime.core.machine.executor.job;

import com.dreamscale.gridtime.core.domain.job.SystemJobType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface SystemJobDescriptor {

    public SystemJobType getJobType();

}

package com.dreamscale.gridtime.core.machine.executor.job;

import com.dreamscale.gridtime.core.domain.job.SystemJobType;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.worker.SystemJobClaim;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CalendarJobDescriptor implements SystemJobDescriptor {

    private LocalDateTime runUntilDate;

    CalendarJobDescriptor(LocalDateTime runUntilDate) {
        this.runUntilDate = runUntilDate;
    }

    @Override
    public SystemJobType getJobType() {
        return SystemJobType.CALENDAR_TILE_GENERATOR;
    }

}

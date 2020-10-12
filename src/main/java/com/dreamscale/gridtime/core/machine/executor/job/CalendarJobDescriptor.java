package com.dreamscale.gridtime.core.machine.executor.job;

import com.dreamscale.gridtime.core.domain.job.SystemJobType;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.worker.SystemJobClaim;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CalendarJobDescriptor implements SystemJobDescriptor {

    private final LocalDateTime calendarJobStart;
    private LocalDateTime runUntilDate;

    CalendarJobDescriptor(LocalDateTime calendarJobStart, LocalDateTime runUntilDate) {
        this.calendarJobStart = calendarJobStart;
        this.runUntilDate = runUntilDate;
    }

    @Override
    public SystemJobType getJobType() {
        return SystemJobType.CALENDAR_TILE_GENERATOR;
    }

}

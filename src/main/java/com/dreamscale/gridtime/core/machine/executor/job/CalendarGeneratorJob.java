package com.dreamscale.gridtime.core.machine.executor.job;

import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.journal.IntentionRepository;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class CalendarGeneratorJob {

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridClock gridClock;

    public CalendarJobDescriptor createJobDescriptor(LocalDateTime now, int daysToKeepAhead) {
        LocalDateTime runUntilDate = calculateRunUntilDate(now, daysToKeepAhead);

        return new CalendarJobDescriptor(getCalendarJobStart(), runUntilDate);
    }

    public Program createStayAheadProgram(CalendarJobDescriptor jobDescriptor) {

        return programFactory.createCalendarGenerator(jobDescriptor);
    }

    private LocalDateTime calculateRunUntilDate(LocalDateTime now, int daysToKeepAhead) {
        LocalDateTime runUntilDate = now.plusDays(daysToKeepAhead);

        return runUntilDate.truncatedTo(ChronoUnit.DAYS);
    }


    private LocalDateTime getCalendarJobStart() {
        GeometryClock.GridTimeSequence lastTwenty = calendarService.getLast(ZoomLevel.TWENTY);

        if (lastTwenty != null) {
            return lastTwenty.getGridTime().panRight().getClockTime();
        } else {
            return gridClock.getGridStart();
        }
    }


    public boolean hasWorkToDo(CalendarJobDescriptor jobDescriptor) {

        GeometryClock.GridTimeSequence lastTwenty = calendarService.getLast(ZoomLevel.TWENTY);

        boolean hasWork = false;

        if (lastTwenty == null) {
            hasWork = true;
        } else {
            LocalDateTime locationOfLastTile = lastTwenty.getGridTime().getClockTime();

            if (locationOfLastTile.isBefore(jobDescriptor.getRunUntilDate())) {
                hasWork = true;
            }

        }

        return hasWork;
    }

}

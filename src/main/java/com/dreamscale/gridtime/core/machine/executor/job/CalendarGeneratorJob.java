package com.dreamscale.gridtime.core.machine.executor.job;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CalendarGeneratorJob {

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    CalendarService calendarService;


    private static final int DAYS_TO_KEEP_AHEAD = 30;

    public CalendarJobDescriptor createJobDescriptor(LocalDateTime now) {
        LocalDateTime runUntilDate = calculateRunUntilDate(now);

        return new CalendarJobDescriptor(runUntilDate);
    }

    public Program createStayAheadProgram(CalendarJobDescriptor jobDescriptor) {

        return programFactory.createCalendarGenerator(jobDescriptor.getRunUntilDate());
    }

    private LocalDateTime calculateRunUntilDate(LocalDateTime now) {
        LocalDateTime runUntilDate = now.plusDays(DAYS_TO_KEEP_AHEAD);

        return runUntilDate.truncatedTo(ChronoUnit.DAYS);
    }

    public boolean hasWorkToDo(LocalDateTime now) {

        GeometryClock.GridTimeSequence lastTwenty = calendarService.getLast(ZoomLevel.TWENTY);

        boolean hasWork = false;

        if (lastTwenty == null) {
            hasWork = true;
        } else {
            LocalDateTime locationOfLastTile = lastTwenty.getGridTime().getClockTime();

            LocalDateTime runUntilDate = calculateRunUntilDate(now);

            if (locationOfLastTile.isBefore(runUntilDate)) {
                hasWork = true;
            }

        }

        return hasWork;
    }

}

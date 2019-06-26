package com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.service.CalendarService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateCalendarTile extends TileInstructions {


    private final CalendarService calendarService;
    private final long tileSequence;
    private final GeometryClock.GridTime gridTime;


    public GenerateCalendarTile(CalendarService calendarService, GeometryClock.GridTime gridTime, long tileSequence ) {
        this.calendarService = calendarService;
        this.gridTime = gridTime;
        this.tileSequence = tileSequence;
    }

    @Override
    public void executeInstruction() throws InterruptedException {

        calendarService.saveCalendar(tileSequence, gridTime);
    }

    @Override
    public String getCmdDescription() {
        return "generate calendar for "+ gridTime.toDisplayString();
    }
}

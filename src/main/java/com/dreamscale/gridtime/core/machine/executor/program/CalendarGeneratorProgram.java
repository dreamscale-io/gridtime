package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.GenerateCalendarTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarGeneratorProgram implements Program {

    private final CalendarService calendarService;


    private int tilesGenerated;
    private final int maxTiles;

    private long twentiesSequence;
    private long dayPartSequence;
    private long daySequence;
    private long weekSequence;

    private Metronome metronome;

    private LocalDateTime calendarEnd;
    private boolean isInitialized;


    public CalendarGeneratorProgram(CalendarService calendarService, int maxTiles) {
        this.calendarService = calendarService;
        this.maxTiles = maxTiles;

        this.isInitialized = false;
    }

    public CalendarGeneratorProgram(CalendarService calendarService, LocalDateTime calendarEnd) {
        this.calendarService = calendarService;
        this.calendarEnd = calendarEnd;
        this.maxTiles = Integer.MAX_VALUE;

        this.isInitialized = false;
    }


    @Override
    public void tick() {
        if (!isInitialized) {
            initMetronomeAndStartSequences();
            isInitialized = true;
        }

        if (!isDone()) {
            metronome.tick();
        }
    }

    @Override
    public boolean isDone() {
        return isInitialized && (metronome.getActiveTick().isAfter(calendarEnd) || tilesGenerated >= maxTiles);
    }

    @Override
    public Metronome.TickScope getActiveTick() {
        return metronome.getActiveTick();
    }

    @Override
    public List<TickInstructions> getInstructionsAtActiveTick() {
        Metronome.TickScope tick = metronome.getActiveTick();

        List<TickInstructions> instructions = new ArrayList<>();

        if (!isDone()) {
            instructions.add(baseTick(tick.getFrom(), tick.getTo()));

            if (tick.hasAggregateTicks()) {
                for (Metronome.TickScope aggregateTick : tick.getAggregateTickScopes()) {
                    instructions.add(aggregateTick(aggregateTick.getFrom(), aggregateTick.getTo()));
                }
            }
        }

        return instructions;
    }

    @Override
    public int getInputQueueDepth() {
        return 0;
    }


    private void initMetronomeAndStartSequences() {
        GeometryClock.GridTimeSequence lastTwenty = calendarService.getLast(ZoomLevel.TWENTY);
        GeometryClock.GridTimeSequence lastDayPart = calendarService.getLast(ZoomLevel.DAY_PART);
        GeometryClock.GridTimeSequence lastDay = calendarService.getLast(ZoomLevel.DAY);
        GeometryClock.GridTimeSequence lastWeek = calendarService.getLast(ZoomLevel.WEEK);

        twentiesSequence = getSequence(lastTwenty);
        dayPartSequence = getSequence(lastDayPart);
        daySequence = getSequence(lastDay);
        weekSequence = getSequence(lastWeek);

        metronome = new Metronome(getCalendarStart(lastTwenty));

        if (calendarEnd == null) {
            calendarEnd = calculateCalendarEnd();
        }
    }


    private LocalDateTime getCalendarStart(GeometryClock.GridTimeSequence lastTwenty) {
        if (lastTwenty != null) {
            return lastTwenty.getGridTime().panRight().getClockTime();
        } else {
            return GeometryClock.getFirstMomentOfYear(2019);
        }
    }

    private LocalDateTime calculateCalendarEnd() {
        int year = calendarService.getNow().getYear();

        return GeometryClock.getFirstMomentOfYear(year + 2);
    }

    private long getSequence(GeometryClock.GridTimeSequence gridTimeSequence) {
        if (gridTimeSequence != null) {
            return gridTimeSequence.getSequenceNumber() + 1;
        } else {
            return 1;
        }
    }


    public TickInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        GenerateCalendarTile instruction = new GenerateCalendarTile(calendarService, fromGridTime, twentiesSequence);
        twentiesSequence++;
        tilesGenerated++;

        return instruction;
    }

    public TickInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        GenerateCalendarTile instruction = null;

        switch (fromGridTime.getZoomLevel()) {
            case DAY_PART:
                instruction = new GenerateCalendarTile(calendarService, fromGridTime, dayPartSequence);
                dayPartSequence++;
                break;
            case DAY:
                instruction = new GenerateCalendarTile(calendarService, fromGridTime, daySequence);
                daySequence++;
                break;
            case WEEK:
                instruction = new GenerateCalendarTile(calendarService, fromGridTime, weekSequence);
                weekSequence++;
                break;
        }

        return instruction;
    }


}

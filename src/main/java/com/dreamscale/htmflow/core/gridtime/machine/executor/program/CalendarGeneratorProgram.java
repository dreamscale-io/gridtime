package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.GenerateCalendarTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService;

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
    public Wire getOutputStreamEventWire() {
        return new DevNullWire();
    }

    @Override
    public Metronome.Tick getActiveTick() {
        return metronome.getActiveTick();
    }

    @Override
    public List<TileInstructions> getInstructionsAtTick(Metronome.Tick tick) {

        List<TileInstructions> instructions = new ArrayList<>();

        if (!isDone()) {
            instructions.add(baseTick(tick.getFrom(), tick.getTo()));

            if (tick.hasAggregateTicks()) {
                for (Metronome.Tick aggregateTick : tick.getAggregateTicks()) {
                    instructions.add(aggregateTick(aggregateTick.getFrom(), aggregateTick.getTo()));
                }
            }
        }

        return instructions;
    }

    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        return getInstructionsAtTick(metronome.getActiveTick());
    }



    private void initMetronomeAndStartSequences() {
        GeometryClock.Sequence lastTwenty = calendarService.getLast(ZoomLevel.TWENTY);
        GeometryClock.Sequence lastDayPart = calendarService.getLast(ZoomLevel.DAY_PART);
        GeometryClock.Sequence lastDay = calendarService.getLast(ZoomLevel.DAY);
        GeometryClock.Sequence lastWeek = calendarService.getLast(ZoomLevel.WEEK);

        twentiesSequence = getSequence(lastTwenty);
        dayPartSequence = getSequence(lastDayPart);
        daySequence = getSequence(lastDay);
        weekSequence = getSequence(lastWeek);

        metronome = new Metronome(getCalendarStart(lastTwenty));

        calendarEnd = calculateCalendarEnd();
    }


    private LocalDateTime getCalendarStart(GeometryClock.Sequence lastTwenty) {
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

    private long getSequence(GeometryClock.Sequence sequence) {
        if (sequence != null) {
            return sequence.getSequenceNumber() + 1;
        } else {
            return 1;
        }
    }


    public TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        GenerateCalendarTile instruction = new GenerateCalendarTile(calendarService, fromGridTime, twentiesSequence);
        twentiesSequence++;
        tilesGenerated++;

        return instruction;
    }

    public TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
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

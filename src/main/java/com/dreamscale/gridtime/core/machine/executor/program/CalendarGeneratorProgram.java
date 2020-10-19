package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.GenerateCalendarTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class CalendarGeneratorProgram implements Program {

    private final CalendarService calendarService;

    private long twentiesSequence;
    private long dayPartSequence;
    private long daySequence;
    private long weekSequence;

    private Metronome metronome;

    private final LocalDateTime calendarJobStart;
    private LocalDateTime runUntilDate;

    private boolean isInitialized;

    public CalendarGeneratorProgram(CalendarService calendarService, LocalDateTime calendarJobStart, LocalDateTime runUntilDate) {
        log.debug("calendar program initialized to run from "+calendarJobStart + " until "+runUntilDate);

        this.calendarService = calendarService;
        this.calendarJobStart = calendarJobStart;
        this.runUntilDate = runUntilDate;

        this.isInitialized = false;
    }

    @Override
    public String getName() {
        return "CalendarGenerator";
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
        return isInitialized && (metronome.getActiveTick().isAfter(runUntilDate) );
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

        log.debug("init metronome: twenties - "+twentiesSequence);
        log.debug("init metronome: daypart - "+dayPartSequence);
        log.debug("init metronome: day - "+daySequence);
        log.debug("init metronome: week - "+weekSequence);

        metronome = new Metronome(calendarJobStart);
    }


    private long getSequence(GeometryClock.GridTimeSequence gridTimeSequence) {
        if (gridTimeSequence != null) {
            return gridTimeSequence.getSequenceNumber() + 1;
        } else {
            return 1;
        }
    }


    public TickInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        log.debug("Creating tick: "+fromGridTime + ", "+twentiesSequence);

        GenerateCalendarTile instruction = new GenerateCalendarTile(calendarService, fromGridTime, twentiesSequence);
        twentiesSequence++;

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

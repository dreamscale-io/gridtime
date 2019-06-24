package com.dreamscale.htmflow.core.gridtime.machine.executor.job;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.GenerateCalendarTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.search.CalendarService;

import java.time.LocalDateTime;

public class CalendarGeneratorJob implements MetronomeJob {

    private final int maxTilesToGenerate;
    private final CalendarService calendarService;

    private long twentiesSequence;
    private long dayPartSequence;
    private long daySequence;
    private long weekSequence;

    private LocalDateTime startPosition;

    private int tilesGenerated = 0;

    public CalendarGeneratorJob(int tilesToGenerate, CalendarService calendarService) {
        this.maxTilesToGenerate = tilesToGenerate;
        this.calendarService = calendarService;

        initStartPositions();
    }

    private void initStartPositions() {
        GeometryClock.Sequence lastTwenty = calendarService.getLast(ZoomLevel.TWENTY);
        GeometryClock.Sequence lastDayPart = calendarService.getLast(ZoomLevel.DAY_PART);
        GeometryClock.Sequence lastDay = calendarService.getLast(ZoomLevel.DAY);
        GeometryClock.Sequence lastWeek = calendarService.getLast(ZoomLevel.WEEK);

        twentiesSequence = getSequence(lastTwenty);
        dayPartSequence = getSequence(lastDayPart);
        daySequence = getSequence(lastDay);
        weekSequence = getSequence(lastWeek);

        startPosition = getCalendarStart(lastTwenty);
    }

    private LocalDateTime getCalendarStart(GeometryClock.Sequence lastTwenty) {
        if (lastTwenty != null) {
            return lastTwenty.getGridTime().panRight().getClockTime();
        } else {
            return GeometryClock.getFirstMomentOfYear(2019);
        }
    }

    private long getSequence(GeometryClock.Sequence sequence) {
        if (sequence != null) {
            return sequence.getSequenceNumber() + 1;
        } else {
            return 1;
        }
    }

    @Override
    public LocalDateTime getStartPosition() {
        return startPosition;
    }

    @Override
    public boolean canTick(LocalDateTime nextPosition) {
        return tilesGenerated < maxTilesToGenerate;
    }

    @Override
    public void gotoPosition(GeometryClock.GridTime gridTime) {
        //do nothing
    }

    @Override
    public TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        GenerateCalendarTile instruction = new GenerateCalendarTile(calendarService, fromGridTime, twentiesSequence);
        twentiesSequence++;

        tilesGenerated++;
        return instruction;
    }

    @Override
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

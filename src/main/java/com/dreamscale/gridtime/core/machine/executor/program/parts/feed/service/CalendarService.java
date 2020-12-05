package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service;

import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.time.*;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.GridtimeSequence;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class CalendarService {

    @Autowired
    GridCalendarRepository gridCalendarRepository;

    @Autowired
    GridClock clock;

    public Long lookupTileSequenceNumber(GeometryClock.GridTime gridTime) {
        Long tileSequence = null;

        GridCalendarEntity calendarTile = gridCalendarRepository.findByZoomLevelAndStartTime(gridTime.getZoomLevel(), gridTime.getClockTime());
        if (calendarTile != null) {
            tileSequence = calendarTile.getTileSeq();
        }

        if (tileSequence == null) {
            throw new RuntimeException("Calendar not found: "+ gridTime.getFormattedGridTime());
        }

        return tileSequence;
    }

    public GridtimeSequence lookupGridTimeSequence(ZoomLevel zoomLevel, long tileSeq) {
        GridCalendarEntity calendarTile = gridCalendarRepository.findByZoomLevelAndTileSeq(zoomLevel, tileSeq);
        if (calendarTile != null) {
            return createGridTimeSequence(zoomLevel, tileSeq, calendarTile.getStartTime());
        } else {
            throw new RuntimeException("Unable to locate calendar for "+zoomLevel + " , sequence "+tileSeq);
        }
    }

    public Long lookupTileSequenceFromSameTime(ZoomLevel zoomInOneLevel, LocalDateTime clockTime) {
        Long tileSequence = null;

        GridCalendarEntity calendarTile = gridCalendarRepository.findByZoomLevelAndStartTime(zoomInOneLevel, clockTime);
        if (calendarTile != null) {
            tileSequence = calendarTile.getTileSeq();
        } else {
            throw new RuntimeException("Unable to locate calendar for "+zoomInOneLevel + " : "+clockTime);
        }


        return tileSequence;
    }

    @Transactional
    public void saveCalendar(long startSeq, int tileCount, GeometryClock.GridTime startTime) {
        long seq = startSeq;
        GeometryClock.GridTime gridTime = startTime;

        for (int i = 0; i < tileCount; i++) {
            saveCalendar(seq, gridTime);
            seq++;
            gridTime = gridTime.panRight();
        }
    }

    @Transactional
    public void saveCalendar(long tileSequence, GeometryClock.GridTime coords) {
        log.info("saveCalendar(" +  tileSequence + ", "+coords.toDisplayString() + ")");

        GridCalendarEntity calendar = new GridCalendarEntity();

        LocalDateTime endTime = coords.panRight().getClockTime().minusSeconds(1);

        calendar.setId(UUID.randomUUID());
        calendar.setTileSeq(tileSequence);
        calendar.setZoomLevel(coords.getZoomLevel());
        calendar.setStartTime(coords.getClockTime());
        calendar.setEndTime(endTime);

        calendar.setGridTime(coords.getFormattedGridTime());

        calendar.setYear(coords.getYear());
        calendar.setBlock(coords.getBlock());
        calendar.setBlockWeek(coords.getBlockWeek());
        calendar.setDay(coords.getDay());
        calendar.setDayPart(coords.getDayPart());
        calendar.setTwentyOfTwelve(coords.getTwentyOfTwelve());

        gridCalendarRepository.save(calendar);
    }

    public GridtimeSequence getLast(ZoomLevel zoomLevel) {
        return toSequence(gridCalendarRepository.getLast(zoomLevel.name()));
    }

    private GridtimeSequence toSequence(GridCalendarEntity tile) {
        if (tile != null) {
            return createGridTimeSequence(tile.getZoomLevel(), tile.getTileSeq(), tile.getStartTime());
        }
        return null;
    }


    private GridtimeSequence createGridTimeSequence(ZoomLevel zoomLevel, long tileSeq, LocalDateTime clockTime) {
        return new GridtimeSequence(tileSeq, GeometryClock.createGridTime(zoomLevel, clockTime));
    }



    public void purgeAll() {
        gridCalendarRepository.truncate();
    }

    public GridCalendarEntity lookupTile(ZoomLevel zoomLevel, LocalDateTime time) {

        Timestamp timestamp = Timestamp.valueOf(time);
        log.debug("Looking up tile for "+zoomLevel.name() + " before "+time + " ("+timestamp + ")");
        return gridCalendarRepository.findTileStartingBeforeTime(zoomLevel.name(), timestamp);
    }
}

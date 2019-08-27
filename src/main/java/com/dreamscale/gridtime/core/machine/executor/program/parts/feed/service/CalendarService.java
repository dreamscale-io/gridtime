package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service;

import com.dreamscale.gridtime.core.domain.time.*;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.service.TimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class CalendarService {

    @Autowired
    GridTimeCalendarRepository gridTimeCalendarRepository;

    @Autowired
    TimeService timeService;

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public Long lookupTileSequenceNumber(GeometryClock.GridTime gridTime) {
        Long tileSequence = null;

        GridTimeCalendarEntity calendarTile = gridTimeCalendarRepository.findByZoomLevelAndClockTime(gridTime.getZoomLevel(), gridTime.getClockTime());
        if (calendarTile != null) {
            tileSequence = calendarTile.getTileSeq();
        }

        return tileSequence;
    }

    public GeometryClock.GridTimeSequence lookupGridTimeSequence(ZoomLevel zoomLevel, long tileSeq) {
        GridTimeCalendarEntity calendarTile = gridTimeCalendarRepository.findByZoomLevelAndTileSeq(zoomLevel, tileSeq);
        if (calendarTile != null) {
            return GeometryClock.createGridTimeSequence(zoomLevel, tileSeq, calendarTile.getClockTime());
        } else {
            log.warn("Unable to locate calendar for "+zoomLevel + " , sequence "+tileSeq);
        }
        return null;
    }

    public Long lookupTileSequenceFromSameTime(ZoomLevel zoomInOneLevel, LocalDateTime clockTime) {
        Long tileSequence = null;

        GridTimeCalendarEntity calendarTile = gridTimeCalendarRepository.findByZoomLevelAndClockTime(zoomInOneLevel, clockTime);
        if (calendarTile != null) {
            tileSequence = calendarTile.getTileSeq();
        } else {
            log.warn("Unable to locate calendar for "+zoomInOneLevel + " : "+clockTime);
        }


        return tileSequence;
    }

    public void saveCalendar(long startSeq, int tileCount, GeometryClock.GridTime startTime) {
        long seq = startSeq;
        GeometryClock.GridTime gridTime = startTime;

        for (int i = 0; i < tileCount; i++) {
            saveCalendar(seq, gridTime);
            seq++;
            gridTime = gridTime.panRight();
        }
    }

    public void saveCalendar(long tileSequence, GeometryClock.GridTime coords) {
        log.info("saveCalendar(" +  tileSequence + ", "+coords.toDisplayString() + ")");

        GridTimeCalendarEntity calendar = new GridTimeCalendarEntity();

        calendar.setId(UUID.randomUUID());
        calendar.setTileSeq(tileSequence);
        calendar.setZoomLevel(coords.getZoomLevel());
        calendar.setClockTime(coords.getClockTime());
        calendar.setGridTime(coords.toDisplayString());

        calendar.setYear(coords.getYear());
        calendar.setBlock(coords.getBlock());
        calendar.setBlockWeek(coords.getBlockWeek());
        calendar.setDay(coords.getDay());
        calendar.setDayPart(coords.getDayPart());
        calendar.setTwentyOfTwelve(coords.getTwentyOfTwelve());

        gridTimeCalendarRepository.save(calendar);
    }

    public GeometryClock.GridTimeSequence getLast(ZoomLevel zoomLevel) {
        return toSequence(gridTimeCalendarRepository.getLast(zoomLevel.name()));
    }

    private GeometryClock.GridTimeSequence toSequence(GridTimeCalendarEntity last) {
        if (last != null) {
            return GeometryClock.createSequencedGridTime(last.getZoomLevel(), last.getClockTime(), last.getTileSeq());
        }
        return null;
    }


}

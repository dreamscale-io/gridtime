package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.service;

import com.dreamscale.htmflow.core.domain.time.*;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalendarService {

    @Autowired
    GridTimeTwentiesRepository gridTimeTwentiesRepository;

    @Autowired
    GridTimeDayPartsRepository gridTimeDayPartsRepository;

    @Autowired
    GridTimeDaysRepository gridTimeDaysRepository;

    @Autowired
    GridTimeWeeksRepository gridTimeWeeksRepository;


    public Long lookupTileSequenceNumber(GeometryClock.GridTime gridTime) {
        Long tileSequence = null;

        switch (gridTime.getZoomLevel()) {
            case TWENTY:
                GridTimeTwentiesEntity twenty = gridTimeTwentiesRepository.findByClockTime(gridTime.getClockTime());
                if (twenty != null) {
                    tileSequence = twenty.getTileSeq();
                }
                break;
            case DAY_PART:
                GridTimeDayPartsEntity dayPart = gridTimeDayPartsRepository.findByClockTime(gridTime.getClockTime());
                if (dayPart != null) {
                    tileSequence = dayPart.getTileSeq();
                }
                break;
            case DAY:
                GridTimeDaysEntity day = gridTimeDaysRepository.findByClockTime(gridTime.getClockTime());
                if (day != null) {
                    tileSequence = day.getTileSeq();
                }
                break;
            case WEEK:
                GridTimeWeeksEntity week = gridTimeWeeksRepository.findByClockTime(gridTime.getClockTime());
                if (week != null) {
                    tileSequence = week.getTileSeq();
                }
        }

        return tileSequence;
    }

    public void saveCalendar(long tileSequence, GeometryClock.GridTime gridTime) {
        switch (gridTime.getZoomLevel()) {
            case TWENTY:
                saveTwenty(tileSequence, gridTime); break;
            case DAY_PART:
                saveDayPart(tileSequence, gridTime); break;
            case DAY:
                saveDay(tileSequence, gridTime); break;
            case WEEK:
                saveWeek(tileSequence, gridTime); break;
        }
    }

    public GeometryClock.Sequence getLast(ZoomLevel zoomLevel) {
        switch (zoomLevel) {
            case TWENTY:
                return toSequence(gridTimeTwentiesRepository.getLast());
            case DAY_PART:
                return toSequence(gridTimeDayPartsRepository.getLast());
            case DAY:
                return toSequence(gridTimeDaysRepository.getLast());
            case WEEK:
                return toSequence(gridTimeWeeksRepository.getLast());
        }
        return null;
    }

    private GeometryClock.Sequence toSequence(GridTimeWeeksEntity last) {
        if (last != null) {
            return GeometryClock.createSequencedGridTime(ZoomLevel.WEEK, last.getClockTime(), last.getTileSeq());
        }
        return null;
    }

    private GeometryClock.Sequence toSequence(GridTimeDaysEntity last) {
        if (last != null) {
            return GeometryClock.createSequencedGridTime(ZoomLevel.DAY, last.getClockTime(), last.getTileSeq());
        }
        return null;
    }

    private GeometryClock.Sequence toSequence(GridTimeDayPartsEntity last) {
        if (last != null) {
            return GeometryClock.createSequencedGridTime(ZoomLevel.DAY_PART, last.getClockTime(), last.getTileSeq());
        }
        return null;
    }

    private GeometryClock.Sequence toSequence(GridTimeTwentiesEntity last) {
        if (last != null) {
            return GeometryClock.createSequencedGridTime(ZoomLevel.TWENTY, last.getClockTime(), last.getTileSeq());
        }
        return null;
    }

    private void saveTwenty(long tileSequence, GeometryClock.GridTime coords) {
        GridTimeTwentiesEntity twenty = new GridTimeTwentiesEntity();

        twenty.setTileSeq(tileSequence);
        twenty.setClockTime(coords.getClockTime());
        twenty.setGridTime(coords.toDisplayString());

        twenty.setYear(coords.getYear());
        twenty.setBlock(coords.getBlock());
        twenty.setBlockWeek(coords.getBlockWeek());
        twenty.setDay(coords.getDay());
        twenty.setDayPart(coords.getDayPart());
        twenty.setTwentyOfTwelve(coords.getTwentyOfTwelve());

        gridTimeTwentiesRepository.save(twenty);
    }

    private void saveWeek(long tileSequence, GeometryClock.GridTime coords) {
        GridTimeWeeksEntity week = new GridTimeWeeksEntity();

        week.setTileSeq(tileSequence);
        week.setClockTime(coords.getClockTime());
        week.setGridTime(coords.toDisplayString());

        week.setYear(coords.getYear());
        week.setBlock(coords.getBlock());
        week.setBlockWeek(coords.getBlockWeek());

        gridTimeWeeksRepository.save(week);
    }

    private void saveDay(long tileSequence, GeometryClock.GridTime coords) {
        GridTimeDaysEntity day = new GridTimeDaysEntity();

        day.setTileSeq(tileSequence);
        day.setClockTime(coords.getClockTime());
        day.setGridTime(coords.toDisplayString());

        day.setYear(coords.getYear());
        day.setBlock(coords.getBlock());
        day.setBlockWeek(coords.getBlockWeek());
        day.setDay(coords.getDay());

        gridTimeDaysRepository.save(day);
    }

    private void saveDayPart(long tileSequence, GeometryClock.GridTime coords) {
        GridTimeDayPartsEntity dayPart = new GridTimeDayPartsEntity();

        dayPart.setTileSeq(tileSequence);
        dayPart.setClockTime(coords.getClockTime());
        dayPart.setGridTime(coords.toDisplayString());

        dayPart.setYear(coords.getYear());
        dayPart.setBlock(coords.getBlock());
        dayPart.setBlockWeek(coords.getBlockWeek());
        dayPart.setDay(coords.getDay());
        dayPart.setDayPart(coords.getDayPart());

        gridTimeDayPartsRepository.save(dayPart);
    }

}

package com.dreamscale.htmflow.core.gridtime.job;

import com.dreamscale.htmflow.core.domain.time.*;
import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.GridTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class GenerateCalendarJob {

    @Autowired
    GridTimeTwentiesRepository gridTimeTwentiesRepository;

    @Autowired
    GridTimeDayPartsRepository gridTimeDayPartsRepository;

    @Autowired
    GridTimeDaysRepository gridTimeDaysRepository;

    @Autowired
    GridTimeWeeksRepository gridTimeWeeksRepository;


    private void generateCalendarTilesStartingFromYear(int startYear, int maxTiles) {

        LocalDateTime firstDay = GeometryClock.getFirstMomentOfYear(startYear);
        GeometryClock clock = new GeometryClock(firstDay);

        generateTiles(maxTiles, clock.getActiveGridTime(), 1);

        Object dayParts = clock.getActiveGridTime().zoomOut();
    }



    private void resumeCalendarTiles(int maxTiles) {

        GridTimeTwentiesEntity lastTwenty = getLastTwenty();
        GeometryClock clock = new GeometryClock(lastTwenty.getClockTime());

        generateTiles(maxTiles,  clock.getActiveGridTime().panRight(), lastTwenty.getTileSeq() + 1);
    }


    private void generateTiles(int maxTilesToGenerate, GeometryClock.GridTime startGridTime, long startSequence) {

        GeometryClock.GridTime gridTime = startGridTime;
        long sequenceNumber = startSequence;

        int tilesGenerated = 0;

        while (tilesGenerated < maxTilesToGenerate) {
            save(sequenceNumber, gridTime);

            sequenceNumber++;
            tilesGenerated++;
            gridTime = gridTime.panRight();

            if (tilesGenerated % 72 == 0) {
                log.debug("Generating grid time for " + gridTime.getFormattedGridTime());
            }
        }
    }

    private GridTimeTwentiesEntity getLastTwenty() {
        return gridTimeTwentiesRepository.getLast();
    }

    private int getTileSequenceStart() {
        return 1;
    }

    private void save(long tileSequence, GeometryClock.GridTime coords) {
        GridTimeTwentiesEntity twenty = new GridTimeTwentiesEntity();

        twenty.setTileSeq(tileSequence);
        twenty.setClockTime(coords.getClockTime());
        twenty.setGridTime(coords.getFormattedGridTime());

        twenty.setYear(coords.getYear());
        twenty.setBlock(coords.getBlock());
        twenty.setBlockWeek(coords.getBlockWeek());
        twenty.setDay(coords.getDay());
        twenty.setDayPart(coords.getDayPart());
        twenty.setTwentyOfTwelve(coords.getTwentyOfTwelve());

        gridTimeTwentiesRepository.save(twenty);
    }


}

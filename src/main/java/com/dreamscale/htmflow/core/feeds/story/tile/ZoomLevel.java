package com.dreamscale.htmflow.core.feeds.story.tile;

import java.time.Duration;

public enum ZoomLevel {

    TWENTY(20, 5, Duration.ofMinutes(20), Twenty.class),
    TWELVE_TWENTIES(12, 3, Duration.ofHours(4), TwelveTwenties.class),
    WORK_DAY_OF_SIX_TWELVES(6, 3, Duration.ofDays(1), WorkDayOfSixTwelves.class),
    WORK_WEEK(7, 1, Duration.ofDays(7), WorkWeek.class),
    BLOCK_OF_SIX_WEEKS(6, 2, Duration.ofDays(42), BlockOfSixWeeks.class),
    YEAR_OF_NINE_BLOCKS(9, 3, Duration.ofDays(365), YearOfNineBlocks.class);

    private final int numberBeatsToAggregate;
    private final int beatsPerPartialSum;

    private final Duration tileDuration;
    private final Class<? extends Tile> tileClazz;


    //1,2,3 AM, 4,5,6 PM

    //1 AM : +3:20 (8am)
    //2 AM : +2:20 (12pm)
    //3 AM : +1:40 (4pm)

    //4 PM : +2:20 (8pm)
    //5 PM : +1:    (12pm)
    //6 PM : +1:20 (4am)

    ZoomLevel(int numberBeatsToAggregate, int beatsPerPartialSum, Duration tileDuration, Class<? extends Tile> tileClazz) {
        this.numberBeatsToAggregate = numberBeatsToAggregate;
        this.beatsPerPartialSum = beatsPerPartialSum;
        this.tileClazz = tileClazz;
        this.tileDuration = tileDuration;
    }

    public int getTileBeats() {
        return numberBeatsToAggregate;
    }

    public int getBeatsPerPartialSum() {
        return beatsPerPartialSum;
    }

    public Duration getTileDuration() {
        return tileDuration;
    }

    public ZoomLevel zoomIn() {
        switch (this) {
            case TWENTY:
                return TWENTY;
            case TWELVE_TWENTIES:
                return TWENTY;
            case WORK_DAY_OF_SIX_TWELVES:
                return TWELVE_TWENTIES;
            case WORK_WEEK:
                return WORK_DAY_OF_SIX_TWELVES;
            case BLOCK_OF_SIX_WEEKS:
                return WORK_WEEK;
            case YEAR_OF_NINE_BLOCKS:
                return BLOCK_OF_SIX_WEEKS;
        }
        return TWENTY;
    }

    public ZoomLevel zoomOut() {
        switch (this) {
            case TWENTY:
                return TWELVE_TWENTIES;
            case TWELVE_TWENTIES:
                return WORK_DAY_OF_SIX_TWELVES;
            case WORK_DAY_OF_SIX_TWELVES:
                return WORK_WEEK;
            case WORK_WEEK:
                return BLOCK_OF_SIX_WEEKS;
            case BLOCK_OF_SIX_WEEKS:
                return YEAR_OF_NINE_BLOCKS;
            case YEAR_OF_NINE_BLOCKS:
                return YEAR_OF_NINE_BLOCKS;
        }
        return YEAR_OF_NINE_BLOCKS;
    }


}

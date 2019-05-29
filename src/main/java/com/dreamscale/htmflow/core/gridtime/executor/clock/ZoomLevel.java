package com.dreamscale.htmflow.core.gridtime.executor.clock;

import java.time.Duration;

public enum ZoomLevel {

    TWENTY(20, 5, Duration.ofMinutes(20)),
    DAY_PART(12, 3, Duration.ofHours(4)),
    DAY(6, 3, Duration.ofDays(1)),
    WORK_WEEK(7, 1, Duration.ofDays(7)),
    BLOCK_OF_SIX_WEEKS(6, 2, Duration.ofDays(42)),
    YEAR(9, 3, Duration.ofDays(365));

    private final int innerBeatsToAggregate;
    private final int beatsPerPartialSum;

    private final Duration tileDuration;

    //1,2,3 AM, 4,5,6 PM

    //1 AM : +3:20 (8am)
    //2 AM : +2:20 (12pm)
    //3 AM : +1:40 (4pm)

    //4 PM : +2:20 (8pm)
    //5 PM : +1:    (12pm)
    //6 PM : +1:20 (4am)

    ZoomLevel(int innerBeatsToAggregate, int beatsPerPartialSum, Duration tileDuration) {
        this.innerBeatsToAggregate = innerBeatsToAggregate;
        this.beatsPerPartialSum = beatsPerPartialSum;
        this.tileDuration = tileDuration;
    }

    public int getInnerBeats() {
        return innerBeatsToAggregate;
    }

    public int getParentBeats() {
        return zoomOut().getInnerBeats();
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
            case DAY_PART:
                return TWENTY;
            case DAY:
                return DAY_PART;
            case WORK_WEEK:
                return DAY;
            case BLOCK_OF_SIX_WEEKS:
                return WORK_WEEK;
            case YEAR:
                return BLOCK_OF_SIX_WEEKS;
        }
        return TWENTY;
    }

    public ZoomLevel zoomOut() {
        switch (this) {
            case TWENTY:
                return DAY_PART;
            case DAY_PART:
                return DAY;
            case DAY:
                return WORK_WEEK;
            case WORK_WEEK:
                return BLOCK_OF_SIX_WEEKS;
            case BLOCK_OF_SIX_WEEKS:
                return YEAR;
            case YEAR:
                return YEAR;
        }
        return YEAR;
    }


}

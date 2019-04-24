package com.dreamscale.htmflow.core.feeds.clock;

public enum ZoomLevel {

    TWENTY_MINS(20), FOUR_HOURS(12), DAYS(6), WEEKS(7), BLOCKS(6), YEAR(9);

    private final int bucketsToAggregate;

    ZoomLevel(int bucketsToAggregate) {
        this.bucketsToAggregate = bucketsToAggregate;
    }

    public int buckets() {
        return bucketsToAggregate;
    }

    public int getNumberToKeepForParent() {
        return zoomOut().bucketsToAggregate;
    }

    public ZoomLevel zoomIn() {
        switch (this) {
            case TWENTY_MINS:
                return TWENTY_MINS;
            case FOUR_HOURS:
                return TWENTY_MINS;
            case DAYS:
                return FOUR_HOURS;
            case WEEKS:
                return DAYS;
            case BLOCKS:
                return WEEKS;
            case YEAR:
                return BLOCKS;
        }
        return TWENTY_MINS;
    }

    public ZoomLevel zoomOut() {
        switch (this) {
            case TWENTY_MINS:
                return FOUR_HOURS;
            case FOUR_HOURS:
                return DAYS;
            case DAYS:
                return WEEKS;
            case WEEKS:
                return BLOCKS;
            case BLOCKS:
                return YEAR;
            case YEAR:
                return YEAR;
        }
        return YEAR;
    }

}

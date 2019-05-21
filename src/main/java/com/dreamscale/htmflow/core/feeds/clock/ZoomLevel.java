package com.dreamscale.htmflow.core.feeds.clock;

public enum ZoomLevel {

    TWENTIES(20), TWELVE_TWENTIES(12), DAYS(6), WEEKS(7), BLOCKS(6), YEAR(9);

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
            case TWENTIES:
                return TWENTIES;
            case TWELVE_TWENTIES:
                return TWENTIES;
            case DAYS:
                return TWELVE_TWENTIES;
            case WEEKS:
                return DAYS;
            case BLOCKS:
                return WEEKS;
            case YEAR:
                return BLOCKS;
        }
        return TWENTIES;
    }

    public ZoomLevel zoomOut() {
        switch (this) {
            case TWENTIES:
                return TWELVE_TWENTIES;
            case TWELVE_TWENTIES:
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

package com.dreamscale.htmflow.core.feeds.clock;

public enum ZoomLevel {

    MIN_20(20), HOUR_4(12), DAY(6), WEEK(7), BLOCK(16), YEAR(4);

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
            case MIN_20:
                return MIN_20;
            case HOUR_4:
                return MIN_20;
            case DAY:
                return HOUR_4;
            case WEEK:
                return DAY;
            case BLOCK:
                return WEEK;
            case YEAR:
                return BLOCK;
        }
        return MIN_20;
    }

    public ZoomLevel zoomOut() {
        switch (this) {
            case MIN_20:
                return HOUR_4;
            case HOUR_4:
                return DAY;
            case DAY:
                return WEEK;
            case WEEK:
                return BLOCK;
            case BLOCK:
                return YEAR;
            case YEAR:
                return YEAR;
        }
        return YEAR;
    }

}

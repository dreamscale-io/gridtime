package com.dreamscale.ideaflow.core.feeds.common;

public enum ZoomLevel {

    MIN(20), HOUR(3), DAY(24), WEEK(7), BLOCK(16), YEAR(4);

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

    ZoomLevel zoomIn() {
        switch (this) {
            case MIN:
                return MIN;
            case HOUR:
                return MIN;
            case DAY:
                return HOUR;
            case WEEK:
                return DAY;
            case BLOCK:
                return WEEK;
            case YEAR:
                return BLOCK;
        }
        return MIN;
    }

    ZoomLevel zoomOut() {
        switch (this) {
            case MIN:
                return HOUR;
            case HOUR:
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

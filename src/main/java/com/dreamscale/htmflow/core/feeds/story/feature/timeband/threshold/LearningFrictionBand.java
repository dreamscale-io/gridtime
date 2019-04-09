package com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold;

import java.time.LocalDateTime;

public class LearningFrictionBand extends RollingAggregateBand {

    private boolean thresholdExceeded;
    private static final int PROGRESS_THRESHOLD_MODIFICATION_COUNT = 250;

    public LearningFrictionBand(LocalDateTime start, LocalDateTime end) {
        super(start, end);
    }

    @Override
    public void evaluateThreshold() {
        if (getAggregateCandleStick().getTotal() > PROGRESS_THRESHOLD_MODIFICATION_COUNT) {
            this.thresholdExceeded = true;
        } else {
            this.thresholdExceeded = false;
        }
    }
}

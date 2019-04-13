package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.CircleDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.details.FeelsDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.LearningFrictionBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.RollingAggregateBand;

import java.time.LocalDateTime;

public class BandFactory {

    public static TimeBand create(BandLayerType bandLayerType, LocalDateTime start, LocalDateTime end, Details details) {
        switch (bandLayerType) {
            case FRICTION_WTF:
                return new WTFFrictionBand(start, end, (CircleDetails) details);
            case FEELS:
                return new FeelsBand(start, end, (FeelsDetails) details);
            case PAIRING_AUTHORS:
                return new AuthorsBand(start, end, (AuthorDetails) details);

        }
        return new TimeBand(start, end, details);
    }

    public static RollingAggregateBand createRollingBand(BandLayerType bandLayerType, LocalDateTime start, LocalDateTime end) {
        switch (bandLayerType) {
            case FRICTION_LEARNING:
                return new LearningFrictionBand(start, end);

        }
        return new RollingAggregateBand(start, end);
    }
}

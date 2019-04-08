package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.clock.BeatsPerBucket;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ProgressDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;

import java.util.List;

/**
 * Translates the starting and stopping of modification activity, staring vs typing, into learning bands
 * when staring around, and considering progress and flow when regularly typing
 */
public class LearningStateObserver implements FlowObserver {

    private static final int PROGRESS_THRESHOLD_MODIFICATION_COUNT = 250;

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        currentStoryFrame.configureRollingBands(BandLayerType.FRICTION_LEARNING, BeatsPerBucket.QUARTER);

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity)flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {

                    int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

                    currentStoryFrame.addRollingSample(BandLayerType.FRICTION_LEARNING, flowActivity.getStart(), modificationCount);
                }
            }
        }

        //this rolls up all the aggregates
        currentStoryFrame.finishAfterLoad();

        //now that our buckets are all aggregated, determine if each aggregate is over the modification threshold
        //set learning vs progress band types for each interval

        evalateProgressTypeForBands(currentStoryFrame);
    }

    private void evalateProgressTypeForBands(StoryFrame currentStoryFrame) {
        TimeBandLayer bandLayer = currentStoryFrame.getBandLayer(BandLayerType.FRICTION_LEARNING);

        ProgressDetails learningBand = new ProgressDetails(ProgressDetails.Type.LEARNING);
        ProgressDetails progressBand = new ProgressDetails(ProgressDetails.Type.PROGRESS);

        for (TimeBand band: bandLayer.getTimeBands()) {
            RollingAggregateBand rollingBand = (RollingAggregateBand)band;
            CandleStick aggregateCandle = rollingBand.getAggregateCandleStick();

            if (aggregateCandle.getTotal() > PROGRESS_THRESHOLD_MODIFICATION_COUNT) {
                rollingBand.setDetails(progressBand);
            } else {
                rollingBand.setDetails(learningBand);
            }
        }
    }


}

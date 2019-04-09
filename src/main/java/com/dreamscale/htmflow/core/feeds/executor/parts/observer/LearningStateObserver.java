package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.CandleStick;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.LearningFrictionBand;

import java.util.List;

/**
 * Translates the starting and stopping of modification activity, staring vs typing, into learning bands
 * when staring around, and considering progress and flow when regularly typing
 */
public class LearningStateObserver implements FlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity)flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {

                    int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

                    currentStoryFrame.addTypingSampleToAssessLearningFriction(flowActivity.getStart(), modificationCount);
                }
            }
        }

        //this rolls up all the aggregates, and evaluates thresholds
        currentStoryFrame.finishAfterLoad();

    }

}

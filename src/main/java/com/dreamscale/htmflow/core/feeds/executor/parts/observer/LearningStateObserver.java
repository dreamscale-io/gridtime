package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;

import java.util.List;

/**
 * Translates the starting and stopping of modification activity, staring vs typing, into learning bands
 * when staring around, and considering progress and flow when regularly typing
 */
public class LearningStateObserver implements FlowObserver<FlowableFlowActivity> {

    @Override
    public void seeInto(List<FlowableFlowActivity> flowables, TileBuilder tileBuilder) {

        for (Flowable flowable : flowables) {
            FlowActivityEntity flowActivity = flowable.get();

            if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {

                int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

                tileBuilder.addTypingSampleToAssessLearningFriction(flowActivity.getStart(), modificationCount);
            }
        }


        //this rolls up all the aggregates, and evaluates thresholds
        tileBuilder.finishAfterLoad();

    }

}

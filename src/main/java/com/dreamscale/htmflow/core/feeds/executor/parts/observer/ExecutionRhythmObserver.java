package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType;

import java.util.List;

/**
 * Translates the raw execution activity into a set of rhythms within the StoryFrame,
 * looking for patterns of red test failure, then iterate, iterate, iterate, and finally getting to test pass
 * as a series of "execution cycles" that indicate dynamics of friction
 */

public class ExecutionRhythmObserver implements FlowObserver {


    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        Movement movement = currentStoryTile.getLastMovement(RhythmLayerType.EXECUTION_ACTIVITY);
        boolean isRedAndWantingGreen = isRedAndWantingGreen(movement);

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity) flowable;

                if (flowActivity.getActivityType().equals(FlowActivityType.Execution)) {
                    ExecutionDetails executionDetails = createExecutionContext(flowActivity);

                    if (!isRedAndWantingGreen && executionDetails.isRed()) {
                        executionDetails.setFirstRed(true);
                        executionDetails.setIsRedAndWantingGreen(true);
                        isRedAndWantingGreen = true;
                    }

                    //we can execute non-unit tests in between
                    if (isRedAndWantingGreen && !executionDetails.isGreen()) {
                        executionDetails.setIsRedAndWantingGreen(true);
                    }

                    if (isRedAndWantingGreen && executionDetails.isGreen()) {
                        executionDetails.setEndOfReds(true);
                        executionDetails.setIsRedAndWantingGreen(false);
                        isRedAndWantingGreen = false;
                    }

                    currentStoryTile.execute(flowActivity.getStart(), executionDetails);
                }

            }
        }

        currentStoryTile.finishAfterLoad();

    }

    private boolean isRedAndWantingGreen(Movement movement) {
        boolean isRedAndWantingGreen = false;

        if (movement != null) {
            ExecutionDetails executionDetails = ((ExecuteThing) movement).getDetails();
            isRedAndWantingGreen = executionDetails.isRedAndWantingGreen();
        }
        return isRedAndWantingGreen;
    }

    private ExecutionDetails createExecutionContext(FlowActivityEntity flowActivity) {
        ExecutionDetails executionDetails = new ExecutionDetails();
        executionDetails.setDuration(flowActivity.getDuration());

        String processName = flowActivity.getMetadataValue(FlowActivityMetadataField.processName);
        String executionTaskType = flowActivity.getMetadataValue(FlowActivityMetadataField.executionTaskType);
        int exitCode = convertToNumber(flowActivity.getMetadataValue(FlowActivityMetadataField.exitCode));
        boolean isDebug = convertToBoolean(flowActivity.getMetadataValue(FlowActivityMetadataField.isDebug));

        executionDetails.setProcessName(processName);
        executionDetails.setExecutionTaskType(executionTaskType);
        executionDetails.setExitCode(exitCode);
        executionDetails.setIsDebug(isDebug);

        return executionDetails;
    }

    private boolean convertToBoolean(String metadataValue) {
        boolean value = false;

        if (metadataValue != null) {
            value = Boolean.parseBoolean(metadataValue);
        }
        return value;
    }

    private int convertToNumber(String metadataValue) {
        int value = 0;
        if (metadataValue != null) {
            value = Integer.parseInt(metadataValue);
        }
        return value;
    }


}

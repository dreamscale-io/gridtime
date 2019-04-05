package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.ExecutionContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RhythmLayerType;

import java.util.List;

/**
 * Translates the raw execution activity into a set of rhythms within the StoryFrame,
 * looking for patterns of red test failure, then iterate, iterate, iterate, and finally getting to test pass
 * as a series of "execution cycles" that indicate dynamics of friction
 */

public class ExecutionRhythmObserver implements FlowObserver {


    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        Movement movement = currentStoryFrame.getLastMovement(RhythmLayerType.EXECUTION_ACTIVITY);
        boolean isRedAndWantingGreen = isRedAndWantingGreen(movement);

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity) flowable;

                if (flowActivity.getActivityType().equals(FlowActivityType.Execution)) {
                    ExecutionContext executionContext = createExecutionContext(flowActivity);

                    if (!isRedAndWantingGreen && executionContext.isRed()) {
                        executionContext.setFirstRed(true);
                        executionContext.setIsRedAndWantingGreen(true);
                        isRedAndWantingGreen = true;
                    }

                    //we can execute non-unit tests in between
                    if (isRedAndWantingGreen && !executionContext.isGreen()) {
                        executionContext.setIsRedAndWantingGreen(true);
                    }

                    if (isRedAndWantingGreen && executionContext.isGreen()) {
                        executionContext.setEndOfReds(true);
                        executionContext.setIsRedAndWantingGreen(false);
                        isRedAndWantingGreen = false;
                    }

                    currentStoryFrame.execute(flowActivity.getStart(), executionContext);
                }

            }
        }

        currentStoryFrame.finishAfterLoad();

    }

    private boolean isRedAndWantingGreen(Movement movement) {
        boolean isRedAndWantingGreen = false;

        if (movement != null) {
            ExecutionContext executionContext = (ExecutionContext) movement.getReference();
            isRedAndWantingGreen = executionContext.isRedAndWantingGreen();
        }
        return isRedAndWantingGreen;
    }

    private ExecutionContext createExecutionContext(FlowActivityEntity flowActivity) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setDuration(flowActivity.getDuration());

        String processName = flowActivity.getMetadataValue(FlowActivityMetadataField.processName);
        String executionTaskType = flowActivity.getMetadataValue(FlowActivityMetadataField.executionTaskType);
        int exitCode = convertToNumber(flowActivity.getMetadataValue(FlowActivityMetadataField.exitCode));
        boolean isDebug = convertToBoolean(flowActivity.getMetadataValue(FlowActivityMetadataField.isDebug));

        executionContext.setProcessName(processName);
        executionContext.setExecutionTaskType(executionTaskType);
        executionContext.setExitCode(exitCode);
        executionContext.setIsDebug(isDebug);

        return executionContext;
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

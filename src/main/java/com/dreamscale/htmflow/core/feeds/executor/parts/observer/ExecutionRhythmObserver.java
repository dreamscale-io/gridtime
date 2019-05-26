package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates the raw execution activity into a set of rhythms within the StoryTile,
 * looking for patterns of red test failure, then iterate, iterate, iterate, and finally getting to test pass
 * as a series of "execution cycles" that indicate dynamics of friction
 */

public class ExecutionRhythmObserver implements FlowObserver<FlowableFlowActivity> {


    @Override
    public void seeInto(List<FlowableFlowActivity> flowables, TileBuilder tileBuilder) {

        List<ExecutionDetails> executionEventDetails = new ArrayList<>();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableFlowActivity) {
                FlowActivityEntity flowActivity = flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Execution)) {
                    ExecutionDetails executionDetails = createExecutionContext(flowActivity);

                    executionEventDetails.add(executionDetails);
                }

            }
        }

        processExecutionEvents(tileBuilder, executionEventDetails);

        tileBuilder.finishAfterLoad();

    }

    private void processExecutionEvents(TileBuilder tileBuilder, List<ExecutionDetails> executionEventDetails) {
        Movement movement = tileBuilder.getLastMovement(RhythmLayerType.EXECUTION_ACTIVITY);
        boolean isRedAndWantingGreen = isRedAndWantingGreen(movement);
        LocalDateTime lastPosition = getLastPosition(movement, tileBuilder.getStart());

        for (int i = 0; i < executionEventDetails.size(); i++) {

            ExecutionDetails executionDetails = executionEventDetails.get(i);

            if (executionDetails.isRed()) {
                isRedAndWantingGreen = true;
            } else if (executionDetails.isGreen()) {
                isRedAndWantingGreen = false;
            }
            executionDetails.setRedAndWantingGreen(isRedAndWantingGreen);

            Duration durationSinceLastExec = Duration.between(lastPosition, executionDetails.getPosition());
            Duration durationUntilNextExec = Duration.between(executionDetails.getPosition(),
                    getNextPosition(executionEventDetails, i, tileBuilder.getEnd()));

            executionDetails.setDurationSinceLastExecution(durationSinceLastExec);
            executionDetails.setDurationUntilNextExecution(durationUntilNextExec);

            tileBuilder.executeThing(executionDetails.getPosition(), executionDetails);
        }
    }

    private LocalDateTime getNextPosition(List<ExecutionDetails> executionEventDetails, int i, LocalDateTime tileEnd) {
        LocalDateTime nextPosition = tileEnd;

        if (i + 1 < executionEventDetails.size()) {
            ExecutionDetails nextDetails = executionEventDetails.get(i + 1);
            nextPosition = nextDetails.getPosition();
        }

        return nextPosition;
    }

    private LocalDateTime getLastPosition(Movement movement, LocalDateTime tileStart) {
        if (movement != null) {
            return movement.getMoment();
        } else {
            return tileStart;
        }
    }

    private boolean isRedAndWantingGreen(Movement movement) {
        boolean isRedAndWantingGreen = false;

        if (movement != null) {
            ExecutionDetails executionDetails = ((ExecuteThing) movement).getExecutionDetails();
            isRedAndWantingGreen = executionDetails.isRedAndWantingGreen();
        }
        return isRedAndWantingGreen;
    }

    private ExecutionDetails createExecutionContext(FlowActivityEntity flowActivity) {
        ExecutionDetails executionDetails = new ExecutionDetails();
        executionDetails.setPosition(flowActivity.getStart());
        executionDetails.setDuration(flowActivity.getDuration());

        String processName = flowActivity.getMetadataValue(FlowActivityMetadataField.processName);
        String executionTaskType = flowActivity.getMetadataValue(FlowActivityMetadataField.executionTaskType);
        int exitCode = convertToNumber(flowActivity.getMetadataValue(FlowActivityMetadataField.exitCode));
        boolean isDebug = convertToBoolean(flowActivity.getMetadataValue(FlowActivityMetadataField.isDebug));

        executionDetails.setProcessName(processName);
        executionDetails.setExecutionTaskType(executionTaskType);
        executionDetails.setExitCode(exitCode);
        executionDetails.setDebug(isDebug);

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

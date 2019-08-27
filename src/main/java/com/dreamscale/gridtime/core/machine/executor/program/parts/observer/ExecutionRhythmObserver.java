package com.dreamscale.gridtime.core.machine.executor.program.parts.observer;

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityType;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.memory.feature.details.ExecutionEvent;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;

/**
 * Translates the raw execution activity into a set of rhythms within the StoryTile,
 * looking for patterns of red test failure, then iterate, iterate, iterate, and finally getting to test pass
 * as a series of "execution cycles" that indicate dynamics of friction
 */

public class ExecutionRhythmObserver implements FlowObserver<FlowableFlowActivity> {


    @Override
    public void see(Window<FlowableFlowActivity> window, GridTile gridTile) {


        for (Flowable flowable : window.getFlowables()) {
            if (flowable instanceof FlowableFlowActivity) {
                FlowActivityEntity flowActivity = flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Execution)) {
                    ExecutionEvent executionEvent = createExecutionEvent(flowActivity);

                    gridTile.executeThing(executionEvent);
                }

            }
        }
    }

    private ExecutionEvent createExecutionEvent(FlowActivityEntity flowActivity) {
        ExecutionEvent executionEvent = new ExecutionEvent(flowActivity.getId(), flowActivity.getStart(), flowActivity.getDuration());

        String processName = flowActivity.getMetadataValue(FlowActivityMetadataField.processName);
        String executionTaskType = flowActivity.getMetadataValue(FlowActivityMetadataField.executionTaskType);
        int exitCode = convertToNumber(flowActivity.getMetadataValue(FlowActivityMetadataField.exitCode));
        boolean isDebug = convertToBoolean(flowActivity.getMetadataValue(FlowActivityMetadataField.isDebug));

        executionEvent.setProcessName(processName);
        executionEvent.setExecutionTaskType(executionTaskType);
        executionEvent.setExitCode(exitCode);
        executionEvent.setDebug(isDebug);

        return executionEvent;
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

package com.dreamscale.gridtime.core.machine.executor.program.parts.observer;

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.gridtime.core.domain.flow.FlowActivityType;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

/**
 * Identifies the enter and exit of components and builds a model of geographic locality
 * by counting traversals from location to location to estimate a heuristic for organizing "gravity"
 */

@Slf4j
public class ComponentSpaceObserver implements FlowObserver<FlowableFlowActivity> {


    @Override
    public void see(Window<FlowableFlowActivity> window, GridTile gridTile) {

        for (FlowableFlowActivity flowable : window.getFlowables()) {
                FlowActivityEntity flowActivity = flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Editor)) {
                    gotoLocation(gridTile, flowActivity);
                }

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {
                    modifyCurrentLocation(gridTile, flowActivity);
                }
        }
    }

    private void gotoLocation(GridTile gridTile, FlowActivityEntity flowActivity) {

        String locationPath = flowActivity.getMetadataValue(FlowActivityMetadataField.filePath);

        if (locationPath != null) {
            gridTile.gotoLocation(flowActivity.getStart(), locationPath, flowActivity.getDuration());
        } else {
            log.warn("Editor activity with null location for member "+flowActivity.getMemberId());
        }
    }

    private void modifyCurrentLocation(GridTile gridTile, FlowActivityEntity flowActivity) {
        int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

        gridTile.modifyCurrentLocation(flowActivity.getStart(), modificationCount);

    }


}

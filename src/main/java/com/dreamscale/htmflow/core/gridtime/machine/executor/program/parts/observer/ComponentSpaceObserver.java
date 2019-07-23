package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.flowable.FlowableFlowActivity;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

/**
 * Identifies the enter and exit of components and builds a model of geographic locality
 * by counting traversals from location to location to estimate a heuristic for organizing "gravity"
 */

public class ComponentSpaceObserver implements FlowObserver<FlowableFlowActivity> {


    @Override
    public void see(Window<FlowableFlowActivity> window, FeaturePool featurePool) {

        GridTile gridTile = featurePool.getActiveGridTile();

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

        gridTile.gotoLocation(flowActivity.getStart(), locationPath, flowActivity.getDuration());

    }

    private void modifyCurrentLocation(GridTile gridTile, FlowActivityEntity flowActivity) {
        int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

        gridTile.modifyCurrentLocation(flowActivity.getStart(), modificationCount);

    }


}

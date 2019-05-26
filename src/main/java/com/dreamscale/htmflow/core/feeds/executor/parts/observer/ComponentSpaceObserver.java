package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import com.dreamscale.htmflow.core.service.ComponentLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Identifies the enter and exit of components and builds a model of geographic locality
 * by counting traversals from location to location to estimate a heuristic for organizing "gravity"
 */
@Component
public class ComponentSpaceObserver implements FlowObserver<FlowableFlowActivity> {

    @Autowired
    ComponentLookupService componentLookupService;

    @Override
    public void seeInto(List<FlowableFlowActivity> flowables, TileBuilder tileBuilder) {


        for (FlowableFlowActivity flowable : flowables) {
                FlowActivityEntity flowActivity = flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Editor)) {
                    gotoLocation(tileBuilder, flowActivity);
                }

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {
                    modifyCurrentLocation(tileBuilder, flowActivity);
                }

        }

        tileBuilder.finishAfterLoad();

    }

    private void gotoLocation(TileBuilder tileBuilder, FlowActivityEntity flowActivity) {

        UUID projectId = tileBuilder.getContextOfMoment(flowActivity.getStart()).getProjectId();

        String locationPath = flowActivity.getMetadataValue(FlowActivityMetadataField.filePath);
        String component = componentLookupService.lookupComponent(projectId, locationPath);

        tileBuilder.gotoLocation(flowActivity.getStart(), component, locationPath, flowActivity.getDuration());

    }

    private void modifyCurrentLocation(TileBuilder tileBuilder, FlowActivityEntity flowActivity) {
        int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

        tileBuilder.modifyCurrentLocation(flowActivity.getStart(), modificationCount);

    }


}

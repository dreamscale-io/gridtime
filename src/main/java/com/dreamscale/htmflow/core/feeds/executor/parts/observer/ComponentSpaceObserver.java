package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
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
public class ComponentSpaceObserver implements FlowObserver {

    @Autowired
    ComponentLookupService componentLookupService;

    @Override
    public void see(StoryTile storyTile, Window window) {

        List<Flowable> flowables = window.getFlowables();


        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity)flowable.get();

                if (flowActivity.getActivityType().equals(FlowActivityType.Editor)) {
                    gotoLocation(storyTile, flowActivity);
                }

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {
                    modifyCurrentLocation(storyTile, flowActivity);
                }
            }
        }

        storyTile.finishAfterLoad();

    }

    private void gotoLocation(StoryTile storyTile, FlowActivityEntity flowActivity) {

        UUID projectId = storyTile.getContextOfMoment(flowActivity.getStart()).getProjectId();

        String locationPath = flowActivity.getMetadataValue(FlowActivityMetadataField.filePath);
        String component = componentLookupService.lookupComponent(projectId, locationPath);

        storyTile.gotoLocation(flowActivity.getStart(), component, locationPath, flowActivity.getDuration());

    }

    private void modifyCurrentLocation(StoryTile storyTile, FlowActivityEntity flowActivity) {
        int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

        storyTile.modifyCurrentLocation(flowActivity.getStart(), modificationCount);

    }


}

package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginning;
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
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        UUID currentProjectId = getLastOpenProjectId(currentStoryTile);


        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity)flowable;

                if (flowActivity.getActivityType().equals(FlowActivityType.Editor)) {
                    gotoLocation(currentStoryTile, currentProjectId, flowActivity);
                }

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {
                    modifyCurrentLocation(currentStoryTile, flowActivity);
                }
            }
        }

        currentStoryTile.finishAfterLoad();

    }

    private void gotoLocation(StoryTile currentStoryTile, UUID currentProjectId, FlowActivityEntity flowActivity) {

        String locationPath = flowActivity.getMetadataValue(FlowActivityMetadataField.filePath);
        String component = componentLookupService.lookupComponent(currentProjectId, locationPath);

        currentStoryTile.gotoLocation(flowActivity.getStart(), component, locationPath, flowActivity.getDuration());

    }

    private void modifyCurrentLocation(StoryTile currentStoryTile, FlowActivityEntity flowActivity) {
        int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

        currentStoryTile.modifyCurrentLocation(flowActivity.getStart(), modificationCount);

    }

    private UUID getLastOpenProjectId(StoryTile storyTile) {
        UUID lastOpenProjectId = null;

        ContextBeginning lastOpenProject = storyTile.getCurrentContext().getProjectContext();
        if (lastOpenProject != null) {
            lastOpenProjectId = lastOpenProject.getReferenceId();
        }

        return lastOpenProjectId;
    }

}

package com.dreamscale.ideaflow.core.feeds.story.see;

import com.dreamscale.ideaflow.core.domain.flow.FlowActivityEntity;
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityMetadataField;
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityType;
import com.dreamscale.ideaflow.core.feeds.common.Flowable;
import com.dreamscale.ideaflow.core.feeds.story.StoryFrame;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowContextBeginningEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowStructureLevel;
import com.dreamscale.ideaflow.core.service.ComponentLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Identifies the enter and exit of components and builds a model of geographic locality
 * by counting traversals from location to location to estimate a heuristic for organizing "gravity"
 */
@Component
public class ComponentSpaceObserver implements IdeaFlowObserver {

    @Autowired
    ComponentLookupService componentLookupService;

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        UUID currentProjectId = getLastOpenProjectId(currentStoryFrame);


        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowActivityEntity) {
                FlowActivityEntity flowActivity = (FlowActivityEntity)flowable;

                if (flowActivity.getActivityType().equals(FlowActivityType.Editor)) {
                    gotoLocation(currentStoryFrame, currentProjectId, flowActivity);
                }

                if (flowActivity.getActivityType().equals(FlowActivityType.Modification)) {
                    modifyCurrentLocation(currentStoryFrame, flowActivity);
                }
            }
        }

        currentStoryFrame.finishStoryFrameAfterLoad();

    }

    private void gotoLocation(StoryFrame currentStoryFrame, UUID currentProjectId, FlowActivityEntity flowActivity) {

        String locationPath = flowActivity.getMetadataValue(FlowActivityMetadataField.filePath);
        String component = componentLookupService.lookupComponent(currentProjectId, locationPath);

        currentStoryFrame.gotoLocation(flowActivity.getStart(), component, locationPath, flowActivity.getDuration());

    }

    private void modifyCurrentLocation(StoryFrame currentStoryFrame, FlowActivityEntity flowActivity) {
        int modificationCount = Integer.valueOf(flowActivity.getMetadataValue(FlowActivityMetadataField.modificationCount));

        currentStoryFrame.modifyCurrentLocation(flowActivity.getStart(), modificationCount);

    }

    private UUID getLastOpenProjectId(StoryFrame storyFrame) {
        UUID lastOpenProjectId = null;

        IdeaFlowContextBeginningEvent lastOpenProject = storyFrame.getCurrentContext(IdeaFlowStructureLevel.PROJECT);
        if (lastOpenProject != null) {
            lastOpenProjectId = lastOpenProject.getReferenceId();
        }

        return lastOpenProjectId;
    }

}

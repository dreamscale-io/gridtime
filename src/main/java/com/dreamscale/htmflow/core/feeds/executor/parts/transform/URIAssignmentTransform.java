package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class URIAssignmentTransform implements TransformStrategy {

    @Autowired
    URIMapper uriMapper;

    @Override
    public void transform(StoryTile storyTile) {

        populateStaticLocationUris(storyTile);

        populateStaticContextUris(storyTile);

        saveUrisForTemporalFeatures(storyTile);

    }

    private void saveUrisForTemporalFeatures(StoryTile storyTile) {
        List<FlowFeature> features = storyTile.extractTemporalFeatures();

        uriMapper.saveTemporalFeatureUris(features);
    }

    private void populateStaticContextUris(StoryTile storyTile) {
        List<MomentOfContext> allContexts = storyTile.getAllContexts();

        for (MomentOfContext momentOfContext : allContexts) {
            Context projectContext = momentOfContext.getProjectContext();
            Context taskContext = momentOfContext.getTaskContext();
            Context intentionContext = momentOfContext.getTaskContext();

            uriMapper.populateProjectContextUri(projectContext);
            uriMapper.populateTaskContextUri(projectContext, taskContext);
            uriMapper.populateIntentionContextUri(projectContext, taskContext, intentionContext);

        }
    }


    private void populateStaticLocationUris(StoryTile storyTile) {

        List<Movement> movements = storyTile.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES).getMovements();

        for (Movement movement : movements) {

            MomentOfContext momentOfContext = storyTile.getContextOfMoment(movement.getMoment());

            switch (movement.getFlowObjectType()) {
                case MOVEMENT_TO_LOCATION:
                    MoveToLocation moveToLocation = (MoveToLocation)movement;
                    UUID projectId = momentOfContext.getProjectId();
                    uriMapper.populateLocationUri(projectId, moveToLocation.getLocation());
                    uriMapper.populateTraversalUri(projectId, moveToLocation.getTraversal());
                    break;
                case MOVEMENT_TO_BOX:
                    MoveToBox moveToBox = (MoveToBox)movement;
                    uriMapper.populateBoxUri(momentOfContext.getProjectId(), moveToBox.getBox());
                    break;
                case MOVEMENT_ACROSS_BRIDGE:
                    MoveAcrossBridge moveAcrossBridge = (MoveAcrossBridge)movement;
                    uriMapper.populateBridgeUri(momentOfContext.getProjectId(), moveAcrossBridge.getBridge());
                    break;
            }
        }
    }



}

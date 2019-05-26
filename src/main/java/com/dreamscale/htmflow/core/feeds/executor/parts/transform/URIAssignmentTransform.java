package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.story.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
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
    public void transform(TileBuilder tileBuilder) {

        populateStaticLocationUris(tileBuilder);

        populateStaticContextUris(tileBuilder);

        //saveUrisForTemporalFeatures(storyTile);

    }

//    private void saveUrisForTemporalFeatures(StoryTile storyTile) {
//        List<FlowFeature> features = storyTile.extractTemporalFeatures();
//
//        uriMapper.saveTemporalFeatureUris(features);
//    }

    private void populateStaticContextUris(TileBuilder tileBuilder) {
        List<MomentOfContext> allContexts = tileBuilder.getAllContexts();

        for (MomentOfContext momentOfContext : allContexts) {
            Context projectContext = momentOfContext.getProjectContext();
            Context taskContext = momentOfContext.getTaskContext();
            Context intentionContext = momentOfContext.getTaskContext();

            uriMapper.populateProjectContextUri(projectContext);
            uriMapper.populateTaskContextUri(projectContext, taskContext);
            uriMapper.populateIntentionContextUri(projectContext, taskContext, intentionContext);

        }
    }


    private void populateStaticLocationUris(TileBuilder tileBuilder) {

        List<Movement> movements = tileBuilder.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES).getMovements();

        for (Movement movement : movements) {

            MomentOfContext momentOfContext = tileBuilder.getContextOfMoment(movement.getMoment());

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

package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBandLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class URIAssignmentTransform implements FlowTransform {

    @Autowired
    URIMapper uriMapper;

    @Override
    public void transform(StoryFrame storyFrame) {

        String frameUri = storyFrame.getFrameUri();

        populateStaticLocationUris(storyFrame);

        populateUrisForBoxesAndBridges(frameUri, storyFrame.getThoughtStructure());

        populateUrisForRhythmLayers(frameUri, storyFrame.getRhythmLayers());

        populateUrisForBandLayers(frameUri, storyFrame.getBandLayers());

    }


    private void populateStaticLocationUris(StoryFrame storyFrame) {
        //lookup the uris in the context of the rhythms,
        // so we can take project context into account with the location lookups

        List<Movement> movements = storyFrame.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES).getMovements();

        for (Movement movement : movements) {
            switch (movement.getType()) {
                case MOVE_TO_LOCATION:
                    MoveToLocation moveToLocation = (MoveToLocation)movement;
                    UUID projectId = moveToLocation.getContext().getProjectId();
                    uriMapper.populateLocationUri(projectId, moveToLocation.getLocation());
                    uriMapper.populateTraversalUri(projectId, moveToLocation.getTraversal());
                    break;
                case MOVE_TO_BOX:
                    MoveToBox moveToBox = (MoveToBox)movement;
                    uriMapper.populateBoxUri(moveToBox.getContext().getProjectId(), moveToBox.getBox());
                    break;
                case MOVE_ACROSS_BRIDGE:
                    MoveAcrossBridge moveAcrossBridge = (MoveAcrossBridge)movement;
                    uriMapper.populateBridgeUri(moveAcrossBridge.getContext().getProjectId(), moveAcrossBridge.getBridge());
                    break;
                case POST_CIRCLE_MESSAGE:
                    PostCircleMessage postCircleMessage = (PostCircleMessage)movement;
                    uriMapper.populateMessageUri(postCircleMessage.getContext().getProjectId(), postCircleMessage.getMessage());
            }
        }
    }

    private void populateUrisForRhythmLayers(String frameUri, List<RhythmLayer> rhythmLayers) {
        for (RhythmLayer layer : rhythmLayers) {
            String layerUri = uriMapper.populateRhythmLayerUri(frameUri, layer);

            for (Movement movement : layer.getMovements()) {
                uriMapper.populateUriForMovement(layerUri, movement);
            }
        }
    }

    private void populateUrisForBandLayers(String frameUri, List<TimeBandLayer> timeBandLayers) {
        for (TimeBandLayer layer : timeBandLayers) {

            String layerUri = uriMapper.populateBandLayerUri(frameUri, layer);


            List<TimeBand> timeBands = layer.getTimeBands();
            for (TimeBand band : timeBands) {
                uriMapper.populateUriForBand(layerUri, band);
            }
        }
    }

    private void populateUrisForBoxesAndBridges(String frameUri, BoxAndBridgeStructure boxAndBridgeStructure) {
        List<Box> boxes = boxAndBridgeStructure.getBoxes();

        for (Box box: boxes) {
            mapUrisWithinBox(frameUri, box);
        }
    }

    private void mapUrisWithinBox(String frameUri, Box box) {

        String boxUri = box.getUri();

        List<ThoughtBubble> bubbles = box.getThoughtBubbles();

        for (ThoughtBubble bubble : bubbles) {
            String bubbleUri = uriMapper.populateBubbleUri(frameUri, boxUri, bubble.getRelativeSequence(), bubble);

            uriMapper.populateBubbleCenterUri(bubbleUri, bubble.getCenter());
            uriMapper.populateBubbleEntranceUri(bubbleUri, bubble.getEntrance());
            uriMapper.populateBubbleExitUri(bubbleUri, bubble.getExit());

            List<RadialStructure.Ring> rings = bubble.getRings();

            for (RadialStructure.Ring ring : rings) {
                uriMapper.populateRingUri(bubbleUri, ring);

                List<RadialStructure.RingLocation> ringLocations = ring.getRingLocations();

                for (RadialStructure.RingLocation ringLocation : ringLocations) {
                    uriMapper.populateRingLocationUri(bubbleUri, ringLocation);
                }

                populateLinkUris(bubbleUri, ring.getLinksToInnerRing());
                populateLinkUris(bubbleUri, ring.getLinksWithinRing());
            }

            populateLinkUris(bubbleUri, bubble.getLinksFromEntrance());
            populateLinkUris(bubbleUri, bubble.getLinksToExit());

            populateBridgeToBubbleUris(bubble);
        }



    }

    private void populateBridgeToBubbleUris(ThoughtBubble bubble) {
        List<BridgeToBubble> bridgeToBubbles = bubble.getBridgeToBubbles();

        for (BridgeToBubble bridgeToBubble : bridgeToBubbles) {
            uriMapper.populateBridgeToBubbleUri(bubble.getUri(), bridgeToBubble);
        }
    }

    private void populateLinkUris(String bubbleUri, List<RadialStructure.Link> linksWithinRing) {
        for (RadialStructure.Link link : linksWithinRing) {
            uriMapper.populateRingLinkUri(bubbleUri, link);
        }
    }

}

package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
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

        UUID projectId = getLastOpenProjectId(storyFrame);
        String frameUri = storyFrame.getFrameUri();

        populateUrisForBoxesAndBridges(projectId, frameUri, storyFrame.getThoughtStructure());

        populateUrisForRhythmLayers(frameUri, storyFrame.getRhythmLayers());

        populateUrisForBandLayers(frameUri, storyFrame.getBandLayers());

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

    private void populateUrisForBoxesAndBridges(UUID projectId, String frameUri, BoxAndBridgeStructure boxAndBridgeStructure) {
        List<Box> boxes = boxAndBridgeStructure.getBoxes();
        List<Bridge> bridges = boxAndBridgeStructure.getBridges();

        for (Bridge bridge: bridges) {
            uriMapper.populateBridgeUri(projectId, bridge);
        }

        for (Box box: boxes) {
            uriMapper.populateBoxUri(projectId, box.getBoxName(), box);

            mapUrisWithinBox(projectId, frameUri, box);
        }
    }

    private void mapUrisWithinBox(UUID projectId, String frameUri, Box box) {

        String boxRelativePath = box.getRelativePath();
        String boxUri = box.getUri();

        List<ThoughtBubble> bubbles = box.getThoughtBubbles();

        for (ThoughtBubble bubble : bubbles) {
            String bubbleUri = uriMapper.populateBubbleUri(frameUri, boxRelativePath, bubble.getRelativeSequence(), bubble);

            uriMapper.populateBubbleCenterUri(projectId, boxUri, bubbleUri, bubble.getCenter());
            uriMapper.populateBubbleEntranceUri(projectId, boxUri,  bubbleUri, bubble.getEntrance());
            uriMapper.populateBubbleExitUri(projectId, boxUri,  bubbleUri, bubble.getExit());

            List<RadialStructure.Ring> rings = bubble.getRings();

            for (RadialStructure.Ring ring : rings) {
                uriMapper.populateRingUri(bubbleUri, ring);

                List<RadialStructure.RingLocation> ringLocations = ring.getRingLocations();

                for (RadialStructure.RingLocation ringLocation : ringLocations) {
                    uriMapper.populateRingLocationUri(projectId, boxUri,  bubbleUri, ringLocation);
                }

                populateLinkUris(projectId, boxUri, bubbleUri, ring.getLinksToInnerRing());
                populateLinkUris(projectId, boxUri, bubbleUri, ring.getLinksWithinRing());
            }

            populateLinkUris(projectId, boxUri, bubbleUri, bubble.getLinksFromEntrance());
            populateLinkUris(projectId, boxUri, bubbleUri, bubble.getLinksToExit());

            populateBridgeToBubbleUris(bubble);
        }



    }

    private void populateBridgeToBubbleUris(ThoughtBubble bubble) {
        List<BridgeToBubble> bridgeToBubbles = bubble.getBridgeToBubbles();

        for (BridgeToBubble bridgeToBubble : bridgeToBubbles) {
            uriMapper.populateBridgeToBubbleUri(bubble.getUri(), bridgeToBubble);
        }
    }

    private void populateLinkUris(UUID projectId, String boxUri, String bubbleUri, List<RadialStructure.Link> linksWithinRing) {
        for (RadialStructure.Link link : linksWithinRing) {
            uriMapper.populateRingLinkUri(projectId, boxUri, bubbleUri, link);
        }
    }

    private UUID getLastOpenProjectId(StoryFrame storyFrame) {
        UUID lastOpenProjectId = null;

        ContextChangeEvent lastOpenProject = storyFrame.getCurrentContext().getProjectContext();
        if (lastOpenProject != null) {
            lastOpenProjectId = lastOpenProject.getReferenceId();
        }

        return lastOpenProjectId;
    }
}

package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class URIAssignmentTransform implements FlowTransform {

    @Autowired
    URIMapper uriMapper;

    @Override
    public void transform(StoryTile storyTile) {

        UUID projectId = getLastOpenProjectId(storyTile);
        String tileUri = storyTile.getTileUri();

        populateUrisForBoxesAndBridges(projectId, tileUri, storyTile.getThoughtStructure());

        List<RhythmLayer> rhythmLayers = storyTile.getRhythmLayers();

        for (RhythmLayer layer : rhythmLayers) {
            List<Movement> movements = layer.getMovements();

            String layerUri = uriMapper.populateRhythmLayerUri(tileUri, layer);

            for (Movement movement : movements) {
                uriMapper.populateUriForMovement(layerUri, movement);
            }
        }


        //TODO next URI thing is the movements

        //TODO populate URIs in the bands

        //so if I go and put URIs on these things, they need to persist, right now I'm calling finish in the observers,
        //at the story frame level, even though, I really only need to finish a layer, do a partial finish

        //theres also a sense of "rewind" when we re-process these frames, and replay these rhythms

        //all of these different objects in the different tracks of the different types

    }

    private void populateUrisForBoxesAndBridges(UUID projectId, String tileUri, BoxAndBridgeStructure boxAndBridgeStructure) {
        List<Box> boxes = boxAndBridgeStructure.getBoxes();
        List<Bridge> bridges = boxAndBridgeStructure.getBridges();

        for (Bridge bridge: bridges) {
            uriMapper.populateBridgeUri(projectId, bridge.getBridgeKey(), bridge);
        }

        for (Box box: boxes) {
            uriMapper.populateBoxUri(projectId, box.getBoxName(), box);

            mapUrisWithinBox(projectId, tileUri, box);
        }
    }

    private void mapUrisWithinBox(UUID projectId, String tileUri, Box box) {

        String boxRelativePath = box.getRelativePath();
        String boxUri = box.getUri();

        List<ThoughtBubble> bubbles = box.getThoughtBubbles();

        for (ThoughtBubble bubble : bubbles) {
            String bubbleUri = uriMapper.populateBubbleUri(tileUri, boxRelativePath, bubble.getRelativeSequence(), bubble);

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

            populateBoxToBubbleLinkUris(bubble);
        }



    }

    private void populateBoxToBubbleLinkUris(ThoughtBubble bubble) {
        List<BridgeToBubbleLink> bridgeToBubbleLinks = bubble.getBridgeToBubbleLinks();

        for (BridgeToBubbleLink bridgeToBubbleLink : bridgeToBubbleLinks) {
            uriMapper.populateBoxToBubbleLinkUri(bubble.getUri(), bridgeToBubbleLink);
        }
    }

    private void populateLinkUris(UUID projectId, String boxUri, String bubbleUri, List<RadialStructure.Link> linksWithinRing) {
        for (RadialStructure.Link link : linksWithinRing) {
            uriMapper.populateRingLinkUri(projectId, boxUri, bubbleUri, link);
        }
    }

    private UUID getLastOpenProjectId(StoryTile storyTile) {
        UUID lastOpenProjectId = null;

        ContextChangeEvent lastOpenProject = storyTile.getCurrentContext().getProjectContext();
        if (lastOpenProject != null) {
            lastOpenProjectId = lastOpenProject.getReferenceId();
        }

        return lastOpenProjectId;
    }
}

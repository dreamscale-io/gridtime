package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.MessageDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.Timeband;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.RollingAggregateBand;

import java.time.LocalDateTime;
import java.util.*;

public class FeatureFactory {

    private final String tileUri;

    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private Map<UUID, Context> contextReferenceMap = new HashMap<>();

    private final Map<String, LocationInBox> locationMap = new HashMap<>();
    private final Map<String, Traversal> traversalMap = new HashMap<>();
    private Map<String, Box> boxMap = new HashMap<>();

    private Map<Class<? extends FlowFeature>, UUID> typeMap = new HashMap<>();
    private Map<UUID, RelativeSequence> sequenceMap = new HashMap<>();

    private List<FlowFeature> temporalFeatures = new ArrayList<>();

    public FeatureFactory(String tileUri) {
        this.tileUri = tileUri;
    }

    public BridgeActivity createBridgeActivity(Bridge bridge) {
        BridgeActivity bridgeActivity = new BridgeActivity(bridge);

        UUID typeId = findOrCreateTypeId(BridgeActivity.class);
        RelativeSequence sequence = findOrCreateRelativeSequence(typeId);

        bridgeActivity.setRelativeSequence(sequence.next());
        bridgeActivity.setRelativePath("/bridge/" + bridgeActivity.getRelativeSequence());
        bridgeActivity.setUri(tileUri + bridgeActivity.getRelativePath());

        return bridgeActivity;
    }

    public BoxActivity createBoxActivity(Box box) {
        BoxActivity boxActivity = new BoxActivity(box);

        UUID typeId = findOrCreateTypeId(BoxActivity.class);
        RelativeSequence sequence = findOrCreateRelativeSequence(typeId);

        boxActivity.setRelativeSequence(sequence.next());
        boxActivity.setRelativePath("/box/" + boxActivity.getRelativeSequence());
        boxActivity.setUri(tileUri + boxActivity.getRelativePath());

        return boxActivity;
    }

    public ThoughtBubble createBubbleInsideBox(BoxActivity box) {
        ThoughtBubble bubble = new ThoughtBubble();

        RelativeSequence sequence = findOrCreateRelativeSequence(box.getId());
        bubble.setRelativeSequence(sequence.next());
        bubble.setRelativePath("/bubble/" + bubble.getRelativeSequence());
        bubble.setUri(box.getUri() + bubble.getRelativePath());
        return bubble;
    }

    public RhythmLayer createRhythmLayer(RhythmLayerType layerType) {
        RhythmLayer layer = new RhythmLayer(layerType);

        layer.setRelativePath("/rhythm/layer/" + layerType.name());
        layer.setUri(tileUri + layer.getRelativePath());

        return layer;
    }


    public TimebandLayer createTimebandLayer(BandLayerType layerType) {
        TimebandLayer layer = new TimebandLayer(layerType);

        layer.setRelativePath("/timeband/layer/" + layerType.name());
        layer.setUri(tileUri + layer.getRelativePath());

        return layer;
    }

    public ExecuteThing createExecuteThing(LocalDateTime moment, ExecutionDetails executionDetails, ExecuteThing.EventType eventType) {

        ExecuteThing executeThing = new ExecuteThing(moment, executionDetails, eventType);
        temporalFeatures.add(executeThing);

        return executeThing;
    }

    public PostCircleMessage createPostCircleMessage(LocalDateTime moment, MessageDetails messageDetails) {

        PostCircleMessage circleMessage = new PostCircleMessage(moment, messageDetails);
        temporalFeatures.add(circleMessage);

        return circleMessage;
    }

    public Movement createMoveToBox(LocalDateTime moment, Box box) {

        MoveToBox moveToBox = new MoveToBox(moment, box);
        temporalFeatures.add(moveToBox);
        return moveToBox;
    }

    public Movement createMoveToLocation(LocalDateTime moment, LocationInBox location, Traversal lastTraversal) {

        MoveToLocation moveToLocation = new MoveToLocation(moment, location, lastTraversal);
        temporalFeatures.add(moveToLocation);

        return moveToLocation;
    }

    public Movement createMoveAcrossBridge(LocalDateTime moment, Bridge bridgeCrossed) {
        MoveAcrossBridge moveAcrossBridge = new MoveAcrossBridge(moment, bridgeCrossed);
        temporalFeatures.add(moveAcrossBridge);

        return moveAcrossBridge;
    }

    public Timeband createBand(BandLayerType layerType, LocalDateTime start, LocalDateTime end, Details details) {
        Timeband band = BandFactory.create(layerType, start, end, details);

        temporalFeatures.add(band);

        return band;
    }

    public RollingAggregateBand createRollingBand(BandLayerType layerType, LocalDateTime start, LocalDateTime end) {
        RollingAggregateBand band = BandFactory.createRollingBand(layerType, start, end);

        temporalFeatures.add(band);
        return band;
    }

    public void assignAllRingUris(ThoughtBubble bubble) {
        String bubbleUri = bubble.getUri();

        populateBubbleCenterUri(bubbleUri, bubble.getCenter());
        populateBubbleEntranceUri(bubbleUri, bubble.getEntrance());
        populateBubbleExitUri(bubbleUri, bubble.getExit());

        List<ThoughtBubble.Ring> rings = bubble.getRings();

        for (ThoughtBubble.Ring ring : rings) {
            populateRingUri(bubbleUri, ring);

            List<ThoughtBubble.RingLocation> ringLocations = ring.getRingLocations();

            for (ThoughtBubble.RingLocation ringLocation : ringLocations) {
                populateRingLocationUri(bubbleUri, ring, ringLocation);
            }

            populateLinkUris(bubbleUri, ring.getLinksToInnerRing());
            populateLinkUris(bubbleUri, ring.getLinksWithinRing());
        }

        populateLinkUris(bubbleUri, bubble.getLinksFromEntrance());
        populateLinkUris(bubbleUri, bubble.getLinksToExit());

    }

    private void populateLinkUris(String bubbleUri, List<ThoughtBubble.Link> linksWithinRing) {
        for (ThoughtBubble.Link link : linksWithinRing) {
            populateRingLinkUri(bubbleUri, link);
        }
    }


    private void populateBubbleCenterUri(String bubbleUri, ThoughtBubble.RingLocation center) {
        if (center == null) return;

        String relativePath = "/center";
        String uri = bubbleUri + relativePath;

        center.setRelativePath(relativePath);
        center.setUri(uri);
    }

    private void populateBubbleEntranceUri(String bubbleUri, ThoughtBubble.RingLocation entrance) {
        if (entrance == null) return;

        String relativePath = "/entrance";
        String uri = bubbleUri + relativePath;

        entrance.setRelativePath(relativePath);
        entrance.setUri(uri);

    }

    private void populateBubbleExitUri(String bubbleUri, ThoughtBubble.RingLocation exit) {
        if (exit == null) return;

        String relativePath = "/exit";
        String uri = bubbleUri + relativePath;

        exit.setRelativePath(relativePath);
        exit.setUri(uri);

    }


    private void populateRingUri(String bubbleUri, ThoughtBubble.Ring ring) {

        String relativePath = "/ring/" + ring.getRingNumber();
        String uri = bubbleUri + relativePath;

        ring.setUri(uri);
        ring.setRelativePath(relativePath);

    }

    private void populateRingLocationUri(String bubbleUri, ThoughtBubble.Ring ring, ThoughtBubble.RingLocation ringLocation) {

        String relativePath = ring.getRelativePath() + "/slot/" + ringLocation.getSlot();
        String uri = bubbleUri + relativePath;

        ringLocation.setUri(uri);
        ringLocation.setRelativePath(relativePath);

    }

    private void populateRingLinkUri(String bubbleUri, ThoughtBubble.Link link) {
        String relativePath = "/link/from" + link.getFrom().getRelativePath() + "/to" + link.getTo().getRelativePath();
        String uri = bubbleUri + relativePath;

        link.setUri(uri);
        link.setRelativePath(relativePath);
    }


    private UUID findOrCreateTypeId(Class<? extends FlowFeature> clazz) {
        UUID typeId = this.typeMap.get(clazz);

        if (typeId == null) {
            typeId = UUID.randomUUID();
            this.typeMap.put(clazz, typeId);
        }

        return typeId;
    }

    private RelativeSequence findOrCreateRelativeSequence(UUID typeId) {
        RelativeSequence relativeSequence = this.sequenceMap.get(typeId);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(0);
            this.sequenceMap.put(typeId, relativeSequence);
        }
        return relativeSequence;
    }


    public Bridge findOrCreateBridge(LocationInBox fromLocation, LocationInBox toLocation) {
        String fromLocationKey = fromLocation.toKey();
        String toLocationKey = toLocation.toKey();

        String bridgeKey = ObjectKeyMapper.createBridgeKey(fromLocationKey, toLocationKey);

        Bridge bridge = this.bridgeMap.get(bridgeKey);
        if (bridge == null) {
            bridge = new Bridge(bridgeKey, fromLocation, toLocation);
            this.bridgeMap.put(bridgeKey, bridge);

        }
        return bridge;
    }

    public Context findOrCreateContext(ContextChangeEvent event) {
        Context sourceContext = event.getContext();

        Context existingContext = this.contextReferenceMap.get(sourceContext.getObjectId());

        if (existingContext == null) {


            this.contextReferenceMap.put(sourceContext.getObjectId(), sourceContext);
            existingContext = sourceContext;
        }
        return existingContext;

    }




    public Box findOrCreateBox(String boxName) {
        Box box = boxMap.get(boxName);
        if (box == null) {
            box = new Box(boxName);
            boxMap.put(boxName, box);
        }
        return box;
    }

    public LocationInBox findOrCreateLocation(String boxName, String locationPath) {
        String key = ObjectKeyMapper.createBoxLocationKey(boxName, locationPath);
        LocationInBox location = locationMap.get(key);
        if (location == null) {
            location = new LocationInBox(boxName, locationPath);
            locationMap.put(key, location);
        }
        return location;
    }


    public Traversal findOrCreateTraversal(LocationInBox locationA, LocationInBox locationB) {

        String traversalKey = ObjectKeyMapper.createLocationTraversalKey(locationA.toKey(), locationB.toKey());

        Traversal traversal = traversalMap.get(traversalKey);

        if (traversal == null) {
            traversal = new Traversal(locationA, locationB);
            traversalMap.put(traversalKey, traversal);
        }

        return traversal;
    }


    public List<FlowFeature> getAllTemporalFeatures() {
        return temporalFeatures;
    }


    public List<Bridge> getAllBridges() {
        return new ArrayList<>(bridgeMap.values());
    }

}

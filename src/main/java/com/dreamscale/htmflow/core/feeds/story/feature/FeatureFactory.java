package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
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
    private List<FlowFeature> nestedTemporalFeatures = new ArrayList<>();

    public FeatureFactory(String tileUri) {
        this.tileUri = tileUri;
    }

    public BridgeActivity createBridgeActivity(Bridge bridge) {
        BridgeActivity bridgeActivity = new BridgeActivity(bridge);

        UUID typeId = findOrCreateTypeId(BridgeActivity.class);
        RelativeSequence sequence = findOrCreateRelativeSequence(typeId);

        bridgeActivity.setRelativeSequence(sequence.next());
        bridgeActivity.setId(UUID.randomUUID());
        bridgeActivity.setRelativePath("/bridge/" + bridgeActivity.getRelativeSequence());
        bridgeActivity.setUri(tileUri + bridgeActivity.getRelativePath());
        bridgeActivity.setFlowObjectType(FlowObjectType.BRIDGE_ACTIVITY);

        temporalFeatures.add(bridgeActivity);

        return bridgeActivity;
    }

    public BoxActivity createBoxActivity(Box box) {
        BoxActivity boxActivity = new BoxActivity(box);

        UUID typeId = findOrCreateTypeId(BoxActivity.class);
        RelativeSequence sequence = findOrCreateRelativeSequence(typeId);

        boxActivity.setRelativeSequence(sequence.next());
        boxActivity.setId(UUID.randomUUID());
        boxActivity.setRelativePath("/box/" + boxActivity.getRelativeSequence());
        boxActivity.setUri(tileUri + boxActivity.getRelativePath());
        boxActivity.setFlowObjectType(FlowObjectType.BOX_ACTIVITY);

        temporalFeatures.add(boxActivity);

        return boxActivity;
    }

    public ThoughtBubble createBubbleInsideBox(BoxActivity box) {
        ThoughtBubble bubble = new ThoughtBubble();

        RelativeSequence sequence = findOrCreateRelativeSequence(box.getId());
        bubble.setRelativeSequence(sequence.next());
        bubble.setId(UUID.randomUUID());
        bubble.setRelativePath("/bubble/" + bubble.getRelativeSequence());
        bubble.setUri(box.getUri() + bubble.getRelativePath());
        bubble.setFlowObjectType(FlowObjectType.BUBBLE);

        nestedTemporalFeatures.add(bubble);
        return bubble;
    }

    public RhythmLayer createRhythmLayer(RhythmLayerType layerType) {
        RhythmLayer layer = new RhythmLayer(layerType);

        layer.setId(UUID.randomUUID());
        layer.setRelativePath("/rhythm/layer/" + layerType.name());
        layer.setUri(tileUri + layer.getRelativePath());
        layer.setFlowObjectType(FlowObjectType.RHYTHM_LAYER);

        temporalFeatures.add(layer);

        return layer;
    }


    public TimebandLayer createTimebandLayer(BandLayerType layerType) {
        TimebandLayer layer = new TimebandLayer(layerType);

        layer.setId(UUID.randomUUID());
        layer.setRelativePath("/timeband/layer/" + layerType.name());
        layer.setUri(tileUri + layer.getRelativePath());
        layer.setFlowObjectType(FlowObjectType.TIMEBAND_LAYER);

        temporalFeatures.add(layer);

        return layer;
    }

    public ExecuteThing createExecuteThing(LocalDateTime moment, ExecutionDetails executionDetails, ExecuteThing.EventType eventType) {

        ExecuteThing executeThing = new ExecuteThing(moment, executionDetails, eventType);
        executeThing.setId(UUID.randomUUID());
        executeThing.setFlowObjectType(FlowObjectType.MOVEMENT_EXECUTE_THING);

        temporalFeatures.add(executeThing);

        return executeThing;
    }

    public PostCircleMessage createPostCircleMessage(LocalDateTime moment, Message message) {

        PostCircleMessage circleMessage = new PostCircleMessage(moment, message);
        circleMessage.setId(UUID.randomUUID());
        circleMessage.setFlowObjectType(FlowObjectType.MOVEMENT_POST_MESSAGE);

        temporalFeatures.add(circleMessage);

        return circleMessage;
    }

    public Movement createMoveToBox(LocalDateTime moment, Box box) {

        MoveToBox moveToBox = new MoveToBox(moment, box);
        moveToBox.setId(UUID.randomUUID());
        moveToBox.setFlowObjectType(FlowObjectType.MOVEMENT_TO_BOX);

        temporalFeatures.add(moveToBox);
        return moveToBox;
    }

    public Movement createMoveToLocation(LocalDateTime moment, LocationInBox location, Traversal lastTraversal) {

        MoveToLocation moveToLocation = new MoveToLocation(moment, location, lastTraversal);
        moveToLocation.setId(UUID.randomUUID());
        moveToLocation.setFlowObjectType(FlowObjectType.MOVEMENT_TO_LOCATION);

        temporalFeatures.add(moveToLocation);

        return moveToLocation;
    }

    public Movement createMoveAcrossBridge(LocalDateTime moment, Bridge bridgeCrossed) {
        MoveAcrossBridge moveAcrossBridge = new MoveAcrossBridge(moment, bridgeCrossed);
        moveAcrossBridge.setId(UUID.randomUUID());
        moveAcrossBridge.setFlowObjectType(FlowObjectType.MOVEMENT_ACROSS_BRIDGE);

        temporalFeatures.add(moveAcrossBridge);

        return moveAcrossBridge;
    }

    public Timeband createBand(BandLayerType layerType, LocalDateTime start, LocalDateTime end, Details details) {
        Timeband band = BandFactory.create(layerType, start, end, details);
        band.setId(UUID.randomUUID());
        band.setFlowObjectType(FlowObjectType.TIMEBAND);

        temporalFeatures.add(band);

        return band;
    }

    public RollingAggregateBand createRollingBand(BandLayerType layerType, LocalDateTime start, LocalDateTime end) {
        RollingAggregateBand band = BandFactory.createRollingBand(layerType, start, end);
        band.setId(UUID.randomUUID());
        band.setFlowObjectType(FlowObjectType.ROLLING_TIMEBAND);

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
                populateRingLocationUri(bubbleUri, ringLocation);
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

        center.setId(UUID.randomUUID());
        center.setRelativePath(relativePath);
        center.setUri(uri);
        center.setFlowObjectType(FlowObjectType.BUBBLE_CENTER);

        nestedTemporalFeatures.add(center);

    }

    private void populateBubbleEntranceUri(String bubbleUri, ThoughtBubble.RingLocation entrance) {
        if (entrance == null) return;

        String relativePath = "/entrance";
        String uri = bubbleUri + relativePath;

        entrance.setId(UUID.randomUUID());
        entrance.setRelativePath(relativePath);
        entrance.setUri(uri);
        entrance.setFlowObjectType(FlowObjectType.BUBBLE_ENTRANCE);

        nestedTemporalFeatures.add(entrance);
    }

    private void populateBubbleExitUri(String bubbleUri, ThoughtBubble.RingLocation exit) {
        if (exit == null) return;

        String relativePath = "/exit";
        String uri = bubbleUri + relativePath;

        exit.setId(UUID.randomUUID());
        exit.setRelativePath(relativePath);
        exit.setUri(uri);
        exit.setFlowObjectType(FlowObjectType.BUBBLE_EXIT);

        nestedTemporalFeatures.add(exit);
    }


    private void populateRingUri(String bubbleUri, ThoughtBubble.Ring ring) {

        String relativePath = "/ring/" + ring.getRingNumber();
        String uri = bubbleUri + relativePath;

        ring.setUri(uri);
        ring.setId(UUID.randomUUID());
        ring.setRelativePath(relativePath);
        ring.setFlowObjectType(FlowObjectType.BUBBLE_RING);

        nestedTemporalFeatures.add(ring);

    }

    private void populateRingLocationUri(String bubbleUri, ThoughtBubble.RingLocation ringLocation) {

        String relativePath = ringLocation.getRingPath() + "/slot/" + ringLocation.getSlot();
        String uri = bubbleUri + relativePath;

        ringLocation.setUri(uri);
        ringLocation.setId(UUID.randomUUID());
        ringLocation.setRelativePath(relativePath);
        ringLocation.setFlowObjectType(FlowObjectType.BUBBLE_RING_LOCATION);

        nestedTemporalFeatures.add(ringLocation);
    }

    private void populateRingLinkUri(String bubbleUri, ThoughtBubble.Link link) {
        String relativePath = "/link/from" + link.getFrom().getRelativePath() + "/to" + link.getTo().getRelativePath();
        String uri = bubbleUri + relativePath;

        link.setUri(uri);
        link.setId(UUID.randomUUID());
        link.setRelativePath(relativePath);
        link.setFlowObjectType(FlowObjectType.BUBBLE_RING_LINK);

        nestedTemporalFeatures.add(link);
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

        Context existingContext = this.contextReferenceMap.get(sourceContext.getId());

        if (existingContext == null) {
            this.contextReferenceMap.put(sourceContext.getId(), sourceContext);
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
        List<FlowFeature> featureList = new ArrayList<>();
        featureList.addAll(temporalFeatures);
        featureList.addAll(nestedTemporalFeatures);

        return featureList;
    }


    public List<Bridge> getAllBridges() {
        return new ArrayList<>(bridgeMap.values());
    }

}

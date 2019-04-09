package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.domain.uri.*;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Message;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBandLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class URIMapper {

    @Autowired
    UriWithinProjectRepository uriWithinProjectRepository;

    @Autowired
    UriWithinFlowRepository uriWithinFlowRepository;

    public void populateBoxUri(UUID projectId, Box box) {
        if (box.getUri() != null) return;

        String boxKey = StandardizedKeyMapper.createBoxKey(box.getBoxName());
        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/box/";

        UriWithinProjectEntity boxObject = findOrCreateUriObject(projectId, UriObjectType.BOX, boxKey, parentUri, relativePathPrefix);
        box.setId(boxObject.getId());
        box.setUri(boxObject.getUri());
        box.setRelativePath(boxObject.getRelativePath());
    }

    public void populateLocationUri(UUID projectId, LocationInBox location) {
        if (location.getUri() != null) return;

        String locationKey = StandardizedKeyMapper.createLocationKey(location.getLocationPath());
        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/location/";

        UriWithinProjectEntity locationObject = findOrCreateUriObject(projectId, UriObjectType.LOCATION, locationKey, parentUri, relativePathPrefix);
        location.setId(locationObject.getId());
        location.setUri(locationObject.getUri());
        location.setRelativePath(locationObject.getRelativePath());
    }

    public void populateBridgeUri(UUID projectId, Bridge bridge) {
        if (bridge.getUri() != null) return;

        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/bridge/";

        UriWithinProjectEntity bridgeObject = findOrCreateUriObject(projectId, UriObjectType.BRIDGE, bridge.getBridgeKey(), parentUri, relativePathPrefix);
        bridge.setId(bridgeObject.getId());
        bridge.setUri(bridgeObject.getUri());
        bridge.setRelativePath(bridgeObject.getRelativePath());
    }

    public void populateTraversalUri(UUID projectId, Traversal traversal) {
        if (traversal.getUri() != null) return;

        String traversalKey = traversal.toKey();
        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/traversal/";

        UriWithinProjectEntity traversalObject = findOrCreateUriObject(projectId, UriObjectType.TRAVERSAL, traversalKey, parentUri, relativePathPrefix);

        traversal.setId(traversalObject.getId());
        traversal.setUri(traversalObject.getUri());
        traversal.setRelativePath(traversalObject.getRelativePath());
    }

    public void populateMessageUri(UUID projectId, Message message) {
        if (message.getUri() != null) return;

        String parentUri = "/project/" + projectId + "/circle/" + message.getCircleId();
        String relativePath = "/message/" + message.getMessageId();

        UriWithinProjectEntity traversalObject = findOrCreateUriObject(projectId, UriObjectType.CIRCLE_MESSAGE, parentUri, relativePath);

        message.setId(traversalObject.getId());
        message.setUri(traversalObject.getUri());
        message.setRelativePath(traversalObject.getRelativePath());

    }


    private UriWithinProjectEntity findOrCreateUriObject(UUID projectId, UriObjectType objectType, String objectKey, String parentUri, String relativePathPrefix) {

        UriWithinProjectEntity uriObject = uriWithinProjectRepository.findByProjectIdAndObjectTypeAndObjectKey(projectId, objectType, objectKey);
        if (uriObject == null) {
            uriObject = new UriWithinProjectEntity();
            uriObject.setId(UUID.randomUUID());
            uriObject.setProjectId(projectId);
            uriObject.setObjectType(objectType);
            uriObject.setObjectKey(objectKey);
            uriObject.setUri(parentUri + relativePathPrefix + uriObject.getId());
            uriObject.setRelativePath(relativePathPrefix + uriObject.getId());

            uriWithinProjectRepository.save(uriObject);
        }

        return uriObject;
    }

    private UriWithinProjectEntity findOrCreateUriObject(UUID projectId, UriObjectType objectType, String parentUri, String relativePath) {
        String uri = parentUri + relativePath;

        UriWithinProjectEntity uriObject = uriWithinProjectRepository.findByProjectIdAndUri(projectId, uri);
        if (uriObject == null) {
            uriObject = new UriWithinProjectEntity();
            uriObject.setId(UUID.randomUUID());
            uriObject.setProjectId(projectId);
            uriObject.setObjectType(objectType);
            uriObject.setObjectKey(uri);
            uriObject.setUri(uri);
            uriObject.setRelativePath(relativePath);

            uriWithinProjectRepository.save(uriObject);
        }

        return uriObject;
    }

    private UriWithinFlowEntity createFlowUri(String uri, String relativePath, FlowUriObjectType flowUriObjectType) {

        UriWithinFlowEntity flowUri = new UriWithinFlowEntity();
        flowUri.setId(UUID.randomUUID());
        flowUri.setObjectType(flowUriObjectType);
        flowUri.setUri(uri);
        flowUri.setRelativePath(relativePath);

        uriWithinFlowRepository.save(flowUri);

        return flowUri;
    }

    public String populateBubbleUri(String frameUri, String boxUri, int relativeSequence, FlowFeature bubble) {

        String relativePath = "/bubble/" + relativeSequence;
        String uri = frameUri + boxUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE);
        bubble.setUri(flowUri.getUri());
        bubble.setId(flowUri.getId());
        bubble.setRelativePath(relativePath);

        return bubble.getUri();

    }


    public void populateBubbleCenterUri(String bubbleUri, RadialStructure.RingLocation center) {

        String relativePath = "/center";
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_CENTER);
        center.setUri(flowUri.getUri());
        center.setId(flowUri.getId());
        center.setRelativePath(relativePath);
    }

    public void populateBubbleEntranceUri(String bubbleUri, RadialStructure.RingLocation entrance) {

        String relativePath = "/entrance";
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_ENTRANCE);
        entrance.setUri(flowUri.getUri());
        entrance.setId(flowUri.getId());
        entrance.setRelativePath(relativePath);
    }

    public void populateBubbleExitUri(String bubbleUri, RadialStructure.RingLocation exit) {

        String relativePath = "/exit";
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_EXIT);
        exit.setUri(flowUri.getUri());
        exit.setId(flowUri.getId());
        exit.setRelativePath(relativePath);
    }


    public void populateRingUri(String bubbleUri, RadialStructure.Ring ring) {

        String relativePath = "/ring/" + ring.getRingNumber();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_RING);
        ring.setUri(flowUri.getUri());
        ring.setId(flowUri.getId());
        ring.setRelativePath(relativePath);

    }

    public String populateRhythmLayerUri(String frameUri, RhythmLayer layer) {

        String relativePath = "/rhythm/layer/" + layer.getLayerType().name();
        String uri = frameUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.RHYTHM_LAYER);
        layer.setUri(flowUri.getUri());
        layer.setId(flowUri.getId());
        layer.setRelativePath(relativePath);

        return layer.getUri();
    }

    public String populateBandLayerUri(String frameUri, TimeBandLayer layer) {

        String relativePath = "/timeband/layer/" + layer.getLayerType().name();
        String uri = frameUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.TIMEBAND_LAYER);
        layer.setUri(flowUri.getUri());
        layer.setId(flowUri.getId());
        layer.setRelativePath(relativePath);

        return layer.getUri();
    }

    public void populateUriForMovement(String layerUri, Movement movement) {

        String relativePath = "/movement/" + movement.getRelativeOffset();

        String uri = layerUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, movement.getType().getFlowUriObjectType());
        movement.setUri(flowUri.getUri());
        movement.setId(flowUri.getId());
        movement.setRelativePath(relativePath);

    }

    public void populateUriForBand(String layerUri, TimeBand band) {

        String relativePath = "/band/" + band.getRelativeOffset();

        String uri = layerUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.TIMEBAND);
        band.setUri(flowUri.getUri());
        band.setId(flowUri.getId());
        band.setRelativePath(relativePath);
    }

    public void populateBridgeToBubbleUri(String bubbleUri, BridgeToBubble bridgeToBubble) {

        String relativePath = "/bridge/" + bridgeToBubble.getRelativeSequence();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_BRIDGE_LINK);
        bridgeToBubble.setUri(flowUri.getUri());
        bridgeToBubble.setId(flowUri.getId());
        bridgeToBubble.setRelativePath(relativePath);

    }

    public void populateRingLocationUri(String bubbleUri, RadialStructure.RingLocation ringLocation) {

        String relativePath = ringLocation.getRingPath() + "/slot/" + ringLocation.getSlot();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_RING_LOCATION);
        ringLocation.setUri(flowUri.getUri());
        ringLocation.setId(flowUri.getId());
        ringLocation.setRelativePath(relativePath);
    }

    public void populateRingLinkUri(String bubbleUri, RadialStructure.Link link) {
        String traversalPath = link.getTraversal().getRelativePath();
        String relativePath = traversalPath + "/link/from/" + link.getFrom().getRelativePath() + "/to/" + link.getTo().getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowUriObjectType.BUBBLE_RING_LINK);
        link.setUri(flowUri.getUri());
        link.setId(flowUri.getId());
        link.setRelativePath(relativePath);
    }


}

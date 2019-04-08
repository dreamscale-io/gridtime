package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.domain.uri.*;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class URIMapper {

    @Autowired
    UriWithinProjectRepository uriWithinProjectRepository;

    @Autowired
    UriWithinFlowRepository uriWithinFlowRepository;

    public void populateBoxUri(UUID projectId, String boxName, FlowFeature box) {

        String boxKey = StandardizedKeyMapper.createBoxKey(boxName);
        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/box/";

        UriWithinProjectEntity boxObject = findOrCreateUriObject(projectId, UriObjectType.BOX, boxKey, parentUri, relativePathPrefix);
        box.setId(boxObject.getId());
        box.setUri(boxObject.getUri());
        box.setRelativePath(boxObject.getRelativePath());
    }

    public void populateLocationUri(UUID projectId, String boxUri, LocationInBox location) {

        String locationKey = StandardizedKeyMapper.createLocationKey(location.getLocationPath());
        String relativePathPrefix = "/location/";

        UriWithinProjectEntity locationObject = findOrCreateUriObject(projectId, UriObjectType.LOCATION, locationKey, boxUri, relativePathPrefix);
        location.setId(locationObject.getId());
        location.setUri(locationObject.getUri());
        location.setRelativePath(locationObject.getRelativePath());
    }

    public void populateBridgeUri(UUID projectId, String bridgeKey, FlowFeature flowFeature) {

        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/bridge/";

        UriWithinProjectEntity bridgeObject = findOrCreateUriObject(projectId, UriObjectType.BRIDGE, bridgeKey, parentUri, relativePathPrefix);
        flowFeature.setId(bridgeObject.getId());
        flowFeature.setUri(bridgeObject.getUri());
        flowFeature.setRelativePath(bridgeObject.getRelativePath());
    }

    public void populateTraversalUri(UUID projectId, String boxUri, Traversal traversal) {

        String traversalKey = traversal.toKey();
        String relativePathPrefix = "/traversal/";

        UriWithinProjectEntity traversalObject = findOrCreateUriObject(projectId, UriObjectType.TRAVERSAL, traversalKey, boxUri, relativePathPrefix);

        traversal.setId(traversalObject.getId());
        traversal.setUri(traversalObject.getUri());
        traversal.setRelativePath(traversalObject.getRelativePath());
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

    private UriWithinFlowEntity createFlowUri(String uri, String relativePath, FlowObjectType flowObjectType) {

        UriWithinFlowEntity flowUri = new UriWithinFlowEntity();
        flowUri.setId(UUID.randomUUID());
        flowUri.setObjectType(flowObjectType);
        flowUri.setUri(uri);
        flowUri.setRelativePath(relativePath);

        uriWithinFlowRepository.save(flowUri);

        return flowUri;
    }

    public String populateBubbleUri(String tileUri, String relativeBoxPath, int relativeSequence, FlowFeature bubble) {

        String relativePath = "/bubble/"+relativeSequence;
        String uri = tileUri + relativeBoxPath + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE);
        bubble.setUri(flowUri.getUri());
        bubble.setId(flowUri.getId());
        bubble.setRelativePath(relativePath);

        return bubble.getUri();

    }



    public void populateBubbleCenterUri(UUID projectId, String boxUri, String bubbleUri, RadialStructure.RingLocation center) {

        populateLocationUri(projectId, boxUri, center.getLocation());

        String relativePath = "/center"+center.getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_CENTER);
        center.setUri(flowUri.getUri());
        center.setId(flowUri.getId());
        center.setRelativePath(relativePath);
    }

    public void populateBubbleEntranceUri(UUID projectId, String boxUri, String bubbleUri, RadialStructure.RingLocation entrance) {

        populateLocationUri(projectId, boxUri, entrance.getLocation());

        String relativePath = "/entrance"+entrance.getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_ENTRANCE);
        entrance.setUri(flowUri.getUri());
        entrance.setId(flowUri.getId());
        entrance.setRelativePath(relativePath);
    }

    public void populateBubbleExitUri(UUID projectId, String boxUri, String bubbleUri, RadialStructure.RingLocation exit) {
        populateLocationUri(projectId, boxUri, exit.getLocation());

        String relativePath = "/exit"+exit.getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_EXIT);
        exit.setUri(flowUri.getUri());
        exit.setId(flowUri.getId());
        exit.setRelativePath(relativePath);
    }


    public void populateRingUri(String bubbleUri, RadialStructure.Ring ring) {

        String relativePath = "/ring/"+ring.getRingNumber();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_RING);
        ring.setUri(flowUri.getUri());
        ring.setId(flowUri.getId());
        ring.setRelativePath(relativePath);

    }

    public void populateBoxToBubbleLinkUri(String bubbleUri, BridgeToBubbleLink bridgeToBubbleLink) {

        String relativePath = "/link/"+ bridgeToBubbleLink.getRelativeSequence() + bridgeToBubbleLink.getBridge().getRelativePath() + bridgeToBubbleLink.getConnectedLocation().getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_BRIDGE_LINK);
        bridgeToBubbleLink.setUri(flowUri.getUri());
        bridgeToBubbleLink.setId(flowUri.getId());
        bridgeToBubbleLink.setRelativePath(relativePath);

    }

    public void populateRingLocationUri(UUID projectId, String boxUri, String bubbleUri, RadialStructure.RingLocation ringLocation) {
        populateLocationUri(projectId, boxUri, ringLocation.getLocation());

        String relativePath = ringLocation.getRingPath() + "/slot/"+ringLocation.getSlot() + ringLocation.getLocation().getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_RING_LOCATION);
        ringLocation.setUri(flowUri.getUri());
        ringLocation.setId(flowUri.getId());
        ringLocation.setRelativePath(relativePath);
    }

    public void populateRingLinkUri(UUID projectId, String boxUri, String bubbleUri, RadialStructure.Link link) {
        populateTraversalUri(projectId, boxUri, link.getTraversal());

        String traversalPath = link.getTraversal().getRelativePath();
        String relativePath = traversalPath + "/link/from/"+link.getFrom().getRelativePath() + "/to/"+link.getTo().getRelativePath();
        String uri = bubbleUri + relativePath;

        UriWithinFlowEntity flowUri = createFlowUri(uri, relativePath, FlowObjectType.BUBBLE_RING_LINK);
        link.setUri(flowUri.getUri());
        link.setId(flowUri.getId());
        link.setRelativePath(relativePath);
    }


}

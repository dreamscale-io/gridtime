package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.domain.uri.*;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Message;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class URIMapper {

    @Autowired
    UriWithinProjectRepository uriWithinProjectRepository;

    @Autowired
    UriWithinFlowRepository uriWithinFlowRepository;

    public void populateBoxUri(UUID projectId, Box box) {
        if (box.getUri() != null) return;

        String boxKey = ObjectKeyMapper.createBoxKey(box.getBoxName());
        String parentUri = "/project/" + projectId;
        String relativePathPrefix = "/box/";

        UriWithinProjectEntity boxObject = findOrCreateUriObject(projectId, UriObjectType.BOX, boxKey, parentUri, relativePathPrefix);
        box.setId(boxObject.getId());
        box.setUri(boxObject.getUri());
        box.setRelativePath(boxObject.getRelativePath());
    }

    public void populateLocationUri(UUID projectId, LocationInBox location) {
        if (location.getUri() != null) return;

        String locationKey = ObjectKeyMapper.createLocationKey(location.getLocationPath());
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

    public void populateProjectContextUri(Context projectContext) {
        if (projectContext == null || projectContext.getUri() != null) return;

        UUID projectId = projectContext.getId();

        String relativePath = "/project/" + projectId;

        UriWithinProjectEntity projectObject = findOrCreateUriObjectWithFixedId(projectId, UriObjectType.PROJECT_CONTEXT, projectId, "", relativePath);

        projectContext.setId(projectObject.getId());
        projectContext.setUri(projectObject.getUri());
        projectContext.setRelativePath(projectObject.getRelativePath());

    }

    public void populateTaskContextUri(Context projectContext, Context taskContext) {
        if (taskContext == null || taskContext.getUri() != null) return;

        UUID projectId = projectContext.getId();
        UUID taskId = taskContext.getId();

        String parentUri = projectContext.getUri();
        String relativePath = "/task/" + taskId;

        UriWithinProjectEntity taskObject = findOrCreateUriObjectWithFixedId(projectId, UriObjectType.TASK_CONTEXT, taskId, parentUri, relativePath);

        projectContext.setId(taskObject.getId());
        projectContext.setUri(taskObject.getUri());
        projectContext.setRelativePath(taskObject.getRelativePath());

    }

    public void populateIntentionContextUri(Context projectContext, Context taskContext, Context intentionContext) {
        if (intentionContext == null || intentionContext.getUri() != null) return;

        UUID projectId = projectContext.getId();
        UUID taskId = taskContext.getId();
        UUID intentionId = intentionContext.getId();

        String parentUri = taskContext.getUri();
        String relativePath = "/intention/" + intentionId;

        UriWithinProjectEntity intentionObject = findOrCreateUriObjectWithFixedId(projectId, UriObjectType.INTENTION_CONTEXT, intentionId, parentUri, relativePath);

        projectContext.setId(intentionObject.getId());
        projectContext.setUri(intentionObject.getUri());
        projectContext.setRelativePath(intentionObject.getRelativePath());
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

    private UriWithinProjectEntity findOrCreateUriObjectWithFixedId(UUID projectId, UriObjectType objectType, UUID fixedId, String parentUri, String relativePath) {

        String uri = parentUri + relativePath;

        UriWithinProjectEntity uriObject = uriWithinProjectRepository.findByProjectIdAndUri(projectId, uri);
        if (uriObject == null) {
            uriObject = new UriWithinProjectEntity();
            uriObject.setId(fixedId);
            uriObject.setProjectId(projectId);
            uriObject.setObjectType(objectType);
            uriObject.setObjectKey(uri);
            uriObject.setUri(uri);
            uriObject.setRelativePath(relativePath);

            uriWithinProjectRepository.save(uriObject);
        }

        return uriObject;
    }


    public void saveTemporalFeatureUris(List<FlowFeature> features) {
        for (FlowFeature feature : features) {
            UriWithinFlowEntity entity = createFlowUri(feature.getUri(), feature.getRelativePath(), feature.getFlowObjectType());
            feature.setId(entity.getId());
        }
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



    public static String createTorchieFeedUri(UUID torchieId) {
        return "@torchie/"+torchieId;
    }

    public static String createCircleFeedUri(UUID circleId) {
        return "@circle/"+circleId;
    }

    public static String createTileUri(String feedUri, ZoomLevel zoomLevel, GeometryClock.Coords tileCoordinates) {
        return feedUri + "/zoom/"+zoomLevel.name()+"/tile/"+tileCoordinates.formatCoords();
    }


}

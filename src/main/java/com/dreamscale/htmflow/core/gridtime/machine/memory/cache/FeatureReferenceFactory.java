package com.dreamscale.htmflow.core.gridtime.machine.memory.cache;

import com.dreamscale.htmflow.core.gridtime.machine.memory.type.*;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.sink.JSONTransformer;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.*;

import java.util.UUID;

public class FeatureReferenceFactory {

    public static final String UNKNOWN = "[UNKNOWN]";

    public IdeaFlowStateReference createWTFReference(CircleDetails circleDetails) {

        return new IdeaFlowStateReference(IdeaFlowStateType.WTF_STATE, circleDetails.toSearchKey(), circleDetails);
    }

    public IdeaFlowStateReference createIdeaFlowReference(IdeaFlowStateType stateType) {
        return new IdeaFlowStateReference(stateType, stateType.getTypeUri());
    }

    public FeelsReference createFeelsStateReference(Integer feelsRating) {

        FeelsRatingDetails feelsDetails = new FeelsRatingDetails(feelsRating);
        FeelsType feelsType = null;
        if (feelsRating >= 0) {
            feelsType = FeelsType.FEELS_GOOD;
        } else {
            feelsType = FeelsType.FEELS_PAIN;
        }
        return new FeelsReference(feelsType, feelsDetails.toSearchKey(), feelsDetails);
    }

    public PlaceReference createLocationReference(UUID projectId, String locationPath) {
        LocationInBox location = new LocationInBox(projectId, UNKNOWN, locationPath);

        return new PlaceReference(PlaceType.LOCATION_IN_BOX, location.toSearchKey(), location);
    }

    public PlaceReference createBoxReference(UUID projectId, String boxName) {
        Box box = new Box(projectId, boxName);

        return new PlaceReference(PlaceType.BOX, box.toSearchKey(), box);
    }

    public PlaceReference createTraversalReference(LocationInBox fromLocation, LocationInBox toLocation) {

        Traversal traversal = new Traversal(
                fromLocation.getProjectId(),
                fromLocation.getBoxName(), fromLocation.getLocationPath(),
                toLocation.getBoxName(), toLocation.getLocationPath());

        return new PlaceReference(PlaceType.TRAVERSAL_WIRE, traversal.toSearchKey(), traversal);
    }

    public WorkContextReference createWorkContextReference(StructureLevel structureLevel, UUID referenceId, String description) {
        WorkContext workContext = new WorkContext(structureLevel, referenceId, description);

        return new WorkContextReference(WorkContextType.fromLevel(structureLevel), workContext.toSearchKey(), workContext);
    }

    public ExecutionReference createExecutionReference(ExecutionEvent executionEvent) {
        if (executionEvent.isUnitTest()) {
            return new ExecutionReference(ExecutionEventType.TEST, executionEvent.toSearchKey(), executionEvent);
        } else {
            return new ExecutionReference(ExecutionEventType.APP, executionEvent.toSearchKey(), executionEvent);
        }
    }

    private String serialize(FeatureDetails feature) {
        return JSONTransformer.toJson(feature);
    }

    private FeatureDetails deserialize(String json, Class<? extends FeatureDetails> serializationClass) {
        return JSONTransformer.fromJson(json, serializationClass);
    }


    public AuthorsReference createAuthorsReference(AuthorsDetails authorsDetails) {
       int numberAuthors = authorsDetails.getAuthors().size();

        AuthorsType authorsType = AuthorsType.SOLO;
       if (numberAuthors == 2) {
           authorsType = AuthorsType.PAIR;
       } else if (numberAuthors > 2) {
           authorsType = AuthorsType.MOB;
       }

       return new AuthorsReference(authorsType, authorsDetails.toSearchKey(), authorsDetails);
    }



}

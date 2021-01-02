package com.dreamscale.gridtime.core.machine.memory.cache;

import com.dreamscale.gridtime.core.machine.memory.type.*;
import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.*;

import java.util.Map;
import java.util.UUID;

public class FeatureReferenceFactory {

    public FeatureReference createResolvedFeatureReference(UUID featureId, FeatureType featureType, String searchKey, FeatureDetails details) {

        FeatureReference reference = featureType.createReference(searchKey, details);
        reference.resolve(featureId, details);

        return reference;
    }

    public IdeaFlowStateReference createWTFReference(CircuitDetails circuitDetails) {

        return new IdeaFlowStateReference(IdeaFlowStateType.WTF_STATE, circuitDetails.toSearchKey(), circuitDetails);
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

    public PlaceReference createLocationReference(UUID projectId, String boxName, String locationPath) {
        LocationInBox location = new LocationInBox(projectId, boxName, locationPath);

        return new PlaceReference(PlaceType.LOCATION, location.toSearchKey(), location);
    }


    public PlaceReference createBoxReference(UUID projectId, String boxName) {
        Box box = new Box(projectId, boxName);

        return new PlaceReference(PlaceType.BOX, box.toSearchKey(), box);
    }

    public PlaceReference createBoxReferenceFromUri(UUID boxFeatureId, String boxUri) {


        Map<String, String> variables = PlaceType.BOX.parseUri(boxUri);

        String boxName = variables.get(PlaceType.TemplateVariable.BOX_NAME);
        String projectId = variables.get(PlaceType.TemplateVariable.PROJECT_ID);

        return new PlaceReference(boxFeatureId, PlaceType.BOX, new Box(UUID.fromString(projectId), boxName));
    }

    public PlaceReference createTraversalReference(LocationInBox fromLocation, LocationInBox toLocation) {

        PlaceReference traversalReference;

        if (isSameBox(fromLocation, toLocation)) {
            Traversal traversal = new Traversal(
                    fromLocation.getProjectId(),
                    fromLocation.getBoxName(), fromLocation.getLocationPath(), toLocation.getLocationPath());
            traversalReference = new PlaceReference(PlaceType.TRAVERSAL_IN_BOX, traversal.toSearchKey(), traversal);
        } else {
            Bridge bridge = new Bridge(
                    fromLocation.getProjectId(),
                    fromLocation.getBoxName(), fromLocation.getLocationPath(),
                    toLocation.getBoxName(), toLocation.getLocationPath());

            traversalReference = new PlaceReference(PlaceType.BRIDGE_BETWEEN_BOXES, bridge.toSearchKey(), bridge);
        }

        return traversalReference;
    }

    private boolean isSameBox(LocationInBox fromLocation, LocationInBox toLocation) {
        return fromLocation.getBoxName().equals(toLocation.getBoxName());
    }

    public WorkContextReference createWorkContextReference(StructureLevel structureLevel, UUID referenceId, String description) {
        WorkContext workContext = new WorkContext(structureLevel, referenceId, description);

        return new WorkContextReference(WorkContextType.fromLevel(structureLevel), workContext.toSearchKey(), workContext);
    }

    public ExecutionEventReference createExecutionReference(ExecutionEvent executionEvent) {
        if (executionEvent.isUnitTest()) {
            return new ExecutionEventReference(ExecutionEventType.TEST, executionEvent.toSearchKey(), executionEvent);
        } else {
            return new ExecutionEventReference(ExecutionEventType.APP, executionEvent.toSearchKey(), executionEvent);
        }
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

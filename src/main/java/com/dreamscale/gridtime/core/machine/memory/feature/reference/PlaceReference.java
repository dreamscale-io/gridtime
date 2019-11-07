package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PlaceReference extends FeatureReference {

    public PlaceReference(PlaceType placeType, String searchKey, FeatureDetails details) {
        super(UUID.randomUUID(), placeType, searchKey, details, false);
    }

    public PlaceReference(UUID featureId, PlaceType placeType, FeatureDetails details) {
        super(featureId, placeType, details.toSearchKey(), details, true);
    }


    public PlaceType getPlaceType() {
        return (PlaceType)getFeatureType();
    }

    public UUID getProjectId() {

        if (getPlaceType() == PlaceType.BOX) {
            Box box = (Box)getDetails();
            return box.getProjectId();
        }
        if (getPlaceType() == PlaceType.LOCATION) {
            LocationInBox location = (LocationInBox)getDetails();
            return location.getProjectId();
        }
        if (getPlaceType() == PlaceType.TRAVERSAL_IN_BOX) {
            Traversal traversal = (Traversal)getDetails();
            return traversal.getProjectId();
        }

        if (getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
            Bridge bridge = (Bridge) getDetails();
            return bridge.getProjectId();
        }
        return null;
    }


    public String getBoxName() {

        if (getPlaceType() == PlaceType.BOX) {
            Box box = (Box)getDetails();
            return box.getBoxName();
        }
        if (getPlaceType() == PlaceType.LOCATION) {
            LocationInBox location = (LocationInBox)getDetails();
            return location.getBoxName();
        }
        if (getPlaceType() == PlaceType.TRAVERSAL_IN_BOX) {
            Traversal traversal = (Traversal)getDetails();
            return traversal.getBoxName();
        }

        if (getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
            Bridge bridge = (Bridge) getDetails();
            return bridge.getBoxNameA();
        }
        return null;
    }

    public String getBoxNameA() {
        return getBoxName();
    }

    public String getBoxNameB() {
        if (getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
            Bridge bridge = (Bridge) getDetails();
            return bridge.getBoxNameB();
        }
        return null;
    }

    public String getLocationPath() {
        if (getPlaceType() == PlaceType.LOCATION) {
            LocationInBox location = (LocationInBox)getDetails();
            return location.getLocationPath();
        }
        if (getPlaceType() == PlaceType.TRAVERSAL_IN_BOX) {
            Traversal traversal = (Traversal)getDetails();
            return traversal.getLocationPathA();
        }

        if (getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
            Bridge bridge = (Bridge) getDetails();
            return bridge.getLocationPathA();
        }
        return null;
    }

    public String getLocationPathA() {
        return getLocationPath();
    }


    public String getLocationPathB() {
        if (getPlaceType() == PlaceType.TRAVERSAL_IN_BOX) {
            Traversal traversal = (Traversal)getDetails();
            return traversal.getLocationPathB();
        }

        if (getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
            Bridge bridge = (Bridge) getDetails();
            return bridge.getLocationPathB();
        }
        return null;
    }

    public <T> T getPlaceDetails() {
        return (T)getDetails();
    }

    @Override
    public String toDisplayString() {
        return getSearchKey();
    }
}

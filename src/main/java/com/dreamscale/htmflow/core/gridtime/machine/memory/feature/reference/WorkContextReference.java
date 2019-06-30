package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference;

import com.dreamscale.htmflow.core.gridtime.machine.memory.type.WorkContextType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.StructureLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.WorkContext;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WorkContextReference extends FeatureReference {


    public WorkContextReference(WorkContextType workContextType, String searchKey, WorkContext details) {
        super(UUID.randomUUID(), workContextType, searchKey, details, false);
    }

    public WorkContextType getWorkType() {
        return (WorkContextType) getFeatureType();
    }

    public String getDescription() {
        return ((WorkContext)getDetails()).getDescription();
    }

    public UUID getReferenceId() {
        return ((WorkContext)getDetails()).getReferenceId();
    }

    public StructureLevel getStructureLevel() {
        return ((WorkContext)getDetails()).getStructureLevel();
    }

    @Override
    public String toDisplayString() {
        return getDescription();
    }
}

package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;

public interface PlayableCompositeTrack {

    TrackSetName getTrackSetName();

    List<GridRow> toGridRows();

    Set<? extends FeatureReference> getFeatures();

    CarryOverContext getCarryOverContext(String subcontextName);

    void initFromCarryOverContext(CarryOverContext subContext);

}

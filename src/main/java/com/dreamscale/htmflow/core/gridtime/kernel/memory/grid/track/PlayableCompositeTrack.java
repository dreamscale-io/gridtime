package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;

public interface PlayableCompositeTrack {

    TrackSetName getTrackSetName();

    List<GridRow> toGridRows();

    Set<? extends FeatureReference> getFeatures();

    CarryOverContext getCarryOverContext(String subcontextName);

    void initFromCarryOverContext(CarryOverContext subContext);

}

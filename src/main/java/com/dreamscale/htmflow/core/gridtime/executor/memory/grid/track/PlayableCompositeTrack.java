package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;

public interface PlayableCompositeTrack {

    TrackSetName getTrackSetName();

    List<GridRow> toGridRows();

    CarryOverContext getCarryOverContext(String subcontextName);

    void initFromCarryOverContext(CarryOverContext subContext);

}

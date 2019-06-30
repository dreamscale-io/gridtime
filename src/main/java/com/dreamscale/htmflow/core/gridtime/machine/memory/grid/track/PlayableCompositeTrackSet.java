package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;

public interface PlayableCompositeTrackSet {

    TrackSetKey getTrackSetKey();

    void finish();

    List<GridRow> toGridRows();

    Set<? extends FeatureReference> getFeatures();

    CarryOverContext getCarryOverContext(String subcontextName);

    void initFromCarryOverContext(CarryOverContext subContext);

}

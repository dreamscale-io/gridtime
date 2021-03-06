package com.dreamscale.gridtime.core.machine.memory.grid.track;

import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;

public interface PlayableCompositeTrackSet {

    TrackSetKey getTrackSetKey();

    void finish();

    List<GridRow> toGridRows();

    Set<? extends FeatureReference> getFeatures();

    CarryOverContext getCarryOverContext(String subcontextName);

    void initFromCarryOverContext(CarryOverContext subContext);

    void populateBoxWithBeat(RelativeBeat beat, GridMetrics boxMetrics);
}

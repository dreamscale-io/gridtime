package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public interface IMusicGrid {

    ZoomLevel getZoomLevel();

    GridRow getRow(Key rowKey);

    List<GridRow> getAllGridRows();

    MusicGridResults playAllTracks();

    Duration getTotalDuration();

    GridMetrics getGridMetrics(FeatureReference featureReference);

    Set<FeatureReference> getFeaturesOfType(FeatureType featureType);
}

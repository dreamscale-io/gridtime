package com.dreamscale.gridtime.core.machine.memory.grid;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;

import java.time.Duration;
import java.util.List;

public interface IMusicGrid {

    ZoomLevel getZoomLevel();

    GridRow getRow(Key rowKey);

    List<GridRow> getAllGridRows();

    MusicGridResults playAllTracks();

    Duration getTotalDuration();
}

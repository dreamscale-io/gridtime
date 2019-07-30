package com.dreamscale.htmflow.core.gridtime.machine.memory.grid;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.Key;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.util.List;

public interface IMusicGrid {

    GridRow getRow(Key rowKey);

    List<GridRow> getAllGridRows();

    MusicGridResults playAllTracks();

}

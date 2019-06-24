package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;

public interface GridCell extends Observable {

    String toHeaderCell();

    String toValueCell();

    String toHeaderCell(int overrideCellSize);

    String toValueCell(int overrideCellSize);

}

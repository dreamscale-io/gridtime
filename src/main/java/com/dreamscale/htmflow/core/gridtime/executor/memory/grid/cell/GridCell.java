package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Observable;

public interface GridCell extends Observable {

    String toHeaderCell();

    String toValueCell();

    String toHeaderCell(int overrideCellSize);

    String toValueCell(int overrideCellSize);

}

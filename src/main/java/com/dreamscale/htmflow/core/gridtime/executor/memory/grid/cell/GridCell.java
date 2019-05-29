package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

public interface GridCell {

    String getHeaderCell();

    String getValueCell();

    String getHeaderCell(int overrideCellSize);

    String getValueCell(int overrideCellSize);

}

package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns;

import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MusicGridResults implements Results {

    List<String> rowsAsStrings;

    @Override
    public String toDisplayString() {
        String gridOutput = "\n";
        for (String row : rowsAsStrings) {
            gridOutput += row + "\n";
        }

        return gridOutput;
    }
}

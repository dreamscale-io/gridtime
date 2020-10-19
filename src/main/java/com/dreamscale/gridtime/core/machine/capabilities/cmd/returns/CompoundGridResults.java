package com.dreamscale.gridtime.core.machine.capabilities.cmd.returns;

import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.grid.Results;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompoundGridResults implements Results {

    String title;
    List<GridTableResults> gridResultGroups;

    public CompoundGridResults(String title) {
        this.title = title;
        this.gridResultGroups = new ArrayList<>();
    }

    public int getGroupCount() {
        return gridResultGroups.size();
    }

    public GridTableResults getResultGroup(int index) {
        return gridResultGroups.get(index);
    }

    @Override
    public String toDisplayString() {
        String gridOutput = "\n";

        gridOutput += "|" + title + "\n|";

        for (int i = 0; i < gridResultGroups.size(); i++) {

            GridTableResults group = gridResultGroups.get(i);
            gridOutput += group.toDisplayString();

            if (i < gridResultGroups.size() - 1) {
                gridOutput += "|";
            }
        }

        return gridOutput;
    }

    @Override
    public String toString() {
        return toDisplayString();
    }

    public void addGrid(GridTableResults grid) {
        gridResultGroups.add(grid);
    }
}

package com.dreamscale.gridtime.core.machine.capabilities.cmd.returns;

import com.dreamscale.gridtime.core.machine.memory.grid.ZoomGrid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompoundGridResults implements Results {

    String title;
    List<MusicGridResults> gridResultGroups;

    public CompoundGridResults(String title) {
        this.title = title;
        this.gridResultGroups = new ArrayList<>();
    }

    public int getGroupCount() {
        return gridResultGroups.size();
    }

    public MusicGridResults getResultGroup(int index) {
        return gridResultGroups.get(index);
    }

    @Override
    public String toDisplayString() {
        String gridOutput = "\n";

        gridOutput += "|" + title + "\n|";

        for (int i = 0; i < gridResultGroups.size(); i++) {

            MusicGridResults group = gridResultGroups.get(i);
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

    public void addGrid(MusicGridResults grid) {
        gridResultGroups.add(grid);
    }
}

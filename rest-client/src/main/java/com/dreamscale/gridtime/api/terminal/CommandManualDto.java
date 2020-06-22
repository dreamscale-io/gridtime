package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandManualDto {

    private List<CommandGroup> groups;

    private Map<CommandGroup, CommandManualPageDto> manualPagesByGroup;

    public void addPage(CommandGroup group, CommandManualPageDto groupPage) {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.add(group);

        if (manualPagesByGroup == null) {
            manualPagesByGroup = new HashMap<>();
        }

        manualPagesByGroup.put(group, groupPage);
    }


    public String toDisplayString() {
        String out = "";

        for (CommandGroup group : manualPagesByGroup.keySet()) {

            CommandManualPageDto page = manualPagesByGroup.get(group);
            out += page.toDisplayString();
        }

        return out;
    }

    public String toString() {
        return toDisplayString();
    }
}

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
public class CommandManualPageDto {

    private String commandGroup;

    private List<CommandDescriptorDto> commandDescriptors;

    public CommandManualPageDto(String commandGroup) {
        this.commandGroup = commandGroup;
        commandDescriptors = new ArrayList<>();
    }

    public void addCommandDescriptor(CommandDescriptorDto manPage)
    {
        commandDescriptors.add(manPage);
    }

    public String toDisplayString() {
        String out = "";

            out += "\n==========================================================\n" +
                    "Command Group: "+ commandGroup + "\n";

            for (CommandDescriptorDto descriptor : commandDescriptors) {
                out += "\n------------------------------------\n"
                        + descriptor.toDisplayString();
            }

            out += "\n==========================================================\n";

        return out;
    }

    public String toString() {
        return toDisplayString();
    }
}

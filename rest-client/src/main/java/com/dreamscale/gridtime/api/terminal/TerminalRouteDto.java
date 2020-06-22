package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalRouteDto {

    private Command command;

    private String argsTemplate;

    private Map<String, String> optionsHelp;

    public String toDisplayString() {

        String out = "Usage: "+ command.name().toLowerCase() + " " + argsTemplate + "\n";

        for (String option: optionsHelp.keySet()) {
            String help = optionsHelp.get(option);
            out += "\nOption: {" + option + "} :: " + help;
        }

        return out;
    }
}

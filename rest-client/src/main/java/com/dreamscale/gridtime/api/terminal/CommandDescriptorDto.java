package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

@Data
@Builder
@AllArgsConstructor
public class CommandDescriptorDto {

    private Command command;
    private String description;

    List<TerminalRouteDto> terminalRoutes;

    public CommandDescriptorDto() {
        this.terminalRoutes = new ArrayList<>();
    }

    public void addRoute(TerminalRouteDto routeDto)
    {
        terminalRoutes.add(routeDto);
    }

    public String toDisplayString() {
        String out = "Command: "+command;
        out += "\nDescription: " + description;

        for (TerminalRouteDto route: terminalRoutes) {
            out += "\n\n" + route.toDisplayString();
        }
        return out;
    }

    public String toString() {
        return toDisplayString();
    }

}

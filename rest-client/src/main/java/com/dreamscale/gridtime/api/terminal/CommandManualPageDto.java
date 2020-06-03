package com.dreamscale.gridtime.api.terminal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandManualPageDto {

    private Command command;
    private String description;

    List<TerminalRouteDto> terminalRoutes;

    public void addRoute(TerminalRouteDto routeDto)
    {
        if (terminalRoutes == null) {
            terminalRoutes = new ArrayList<>();
        }
        terminalRoutes.add(routeDto);
    }

}

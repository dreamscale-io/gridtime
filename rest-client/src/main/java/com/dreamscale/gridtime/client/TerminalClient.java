package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.terminal.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TerminalClient {


    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH)
    TerminalCircuitDto createCircuit();

    //terminal command manual

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH + ResourcePaths.HELP_PATH)
    CommandManualDto getCommandManual();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH + ResourcePaths.HELP_PATH + "/{commandOrGroupName}")
    CommandManualPageDto getManualPageForCommandOrGroup(@Param("commandOrGroupName") String commandOrGroupName);

    //terminal circuit operating commands

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH +  "/{circuitName}")
    TerminalCircuitDto getCircuit(@Param("circuitName") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH +  "/{circuitName}" + ResourcePaths.RUN_PATH)
    SimpleStatusDto run(@Param("circuitName") String circuitName, CommandInputDto commandInputDto);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH +  "/{circuitName}" + ResourcePaths.SET_PATH)
    SimpleStatusDto set(@Param("circuitName") String circuitName, EnvironmentParamInputDto environmentParamInputDto);


    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH +  "/{circuitName}" + ResourcePaths.JOIN_PATH)
    SimpleStatusDto joinCircuit(@Param("circuitName") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH +  "/{circuitName}" + ResourcePaths.LEAVE_PATH)
    SimpleStatusDto leaveCircuit(@Param("circuitName") String circuitName);


}

package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TerminalClient {


    @RequestLine("POST " + ResourcePaths.TERMINAL_PATH + ResourcePaths.CIRCUIT_PATH)
    TerminalCircuitDto createCircuit();

    @RequestLine("GET " + ResourcePaths.TERMINAL_PATH + ResourcePaths.CIRCUIT_PATH + "/{circuitName}")
    TerminalCircuitDto getCircuit(@Param("circuitName") String circuitName);

    @RequestLine("POST " + ResourcePaths.TERMINAL_PATH + ResourcePaths.CIRCUIT_PATH + "/{circuitName}" + ResourcePaths.RUN_PATH)
    TalkMessageDto runCommand(@Param("circuitName") String circuitName, CommandInputDto commandInputDto);

    @RequestLine("GET " + ResourcePaths.TERMINAL_PATH + ResourcePaths.MANUAL_PATH)
    CommandManualDto getCommandManual();

    @RequestLine("GET " + ResourcePaths.TERMINAL_PATH + ResourcePaths.MANUAL_PATH + ResourcePaths.COMMAND_PATH + "/{commandName}")
    CommandManualPageDto getManualPageForCommand(@Param("commandName") String commandName);

    @RequestLine("GET " + ResourcePaths.TERMINAL_PATH + ResourcePaths.MANUAL_PATH + ResourcePaths.CONTEXT_PATH + "/{commandGroupName}")
    CommandManualPageDto getManualPageForGroup(@Param("commandGroupName") String commandGroupName);

}


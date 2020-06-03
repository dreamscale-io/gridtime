package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.account.UsernameInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.CommandManualDto;
import com.dreamscale.gridtime.api.terminal.CommandManualPageDto;
import com.dreamscale.gridtime.api.terminal.RunCommandInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TerminalClient {

    /**
     * Run a specific command on the grid and return the result synchronously as a TalkMessageDto
     *
     * @see com.dreamscale.gridtime.api.terminal.Command for the available command types
     *
     * @param runCommandInputDto RunCommandInputDto
     * @return TalkMessageDto
     */

    @RequestLine("POST " + ResourcePaths.TERMINAL_PATH + ResourcePaths.RUN_PATH)
    TalkMessageDto runCommand(RunCommandInputDto runCommandInputDto);

    @RequestLine("GET " + ResourcePaths.TERMINAL_PATH + ResourcePaths.MANUAL_PATH)
    CommandManualDto getCommandManual();

    @RequestLine("GET " + ResourcePaths.TERMINAL_PATH + ResourcePaths.MANUAL_PATH + ResourcePaths.COMMAND_PATH + "/{commandName}")
    CommandManualPageDto getCommandManualPage(@Param("commandName") String commandName);

}

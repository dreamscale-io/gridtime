package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.RunCommandInputDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class TerminalCmdRouter {


    public TalkMessageDto runCommand(UUID organizationId, UUID memberId, RunCommandInputDto runCommandInputDto) {

        return null;
    }
}

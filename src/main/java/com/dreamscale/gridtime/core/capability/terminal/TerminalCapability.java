package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.terminal.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class TerminalCapability {

    @Autowired
    private TerminalRouteRegistry terminalRouteRegistry;


    public TerminalCircuitDto createCircuit(UUID organizationId, UUID invokingMemberId) {

        //create terminal circuit table

        //create talk room for the circuit

        //join the member to the circuit as creator

        return null;
    }

    public SimpleStatusDto run(UUID organizationId, UUID invokingMemberId, String sessionName, CommandInputDto commandInputDto) {

        return null;
    }

    public SimpleStatusDto joinCircuit(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public SimpleStatusDto closeSession(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public TerminalCircuitDto getCircuit(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public SimpleStatusDto leaveCircuit(UUID organizationId, UUID invokingMemberId, String circuitName) {
        return null;
    }

    public SimpleStatusDto setEnvironmentParam(UUID organizationId, UUID invokingMemberId, String circuitName, EnvironmentParamInputDto environmentParamInputDto) {
        return null;
    }
}

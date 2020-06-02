package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageMetaProp;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.api.terminal.RunCommandInputDto;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.GridClock;
import com.dreamscale.gridtime.core.service.MemberDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TerminalRouteRegistry {

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    private GridClock gridClock;

    private MultiValueMap<Command, TerminalRoute> terminalRoutes = DefaultCollections.multiMap();

    public TalkMessageDto routeCommand(UUID organizationId, UUID memberId, RunCommandInputDto runCommandInputDto) {

        List<TerminalRoute> terminalRoutes = this.terminalRoutes.get(runCommandInputDto.getCommand());

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        if (terminalRoutes != null) {
            for (TerminalRoute route : terminalRoutes) {

                if (route.matches(runCommandInputDto.getArgumentStr())) {
                    Object result = route.route(runCommandInputDto.getArgumentStr());

                    return wrapResultAsTalkMessage(now, nanoTime, memberId, result);
                }
            }
        }

        throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Unable to find matching terminal route in registry.");
    }

    private TalkMessageDto wrapResultAsTalkMessage(LocalDateTime now, Long nanoTime, UUID memberId, Object result) {

        String requestUri = getRequestUriFromContext();

        TalkMessageDto messageDto = new TalkMessageDto();
        messageDto.setId(UUID.randomUUID());
        messageDto.setRequest(requestUri);
        messageDto.addMetaProp(TalkMessageMetaProp.FROM_MEMBER_ID, memberId.toString());

        MemberDetailsEntity memberDetails = memberDetailsService.lookupMemberDetails(memberId);

        if (memberDetails != null) {
            messageDto.addMetaProp(TalkMessageMetaProp.FROM_USERNAME, memberDetails.getUsername());
            messageDto.addMetaProp(TalkMessageMetaProp.FROM_FULLNAME, memberDetails.getFullName());
        }

        messageDto.setMessageTime(now);
        messageDto.setNanoTime(nanoTime);

        if (result != null) {
            messageDto.setMessageType(result.getClass().getSimpleName());
            messageDto.setData(result);
        }

        return messageDto;
    }

    private String getRequestUriFromContext() {

        RequestContext context = RequestContext.get();

        if (context != null) {
            return context.getRequestUri();
        } else {
            return null;
        }
    }

    public void register(Command command, TerminalRoute terminalRoute) {
        log.info("[TerminalRouteRegistry] Register "+command.name());

        terminalRoutes.add(command, terminalRoute);
    }
}

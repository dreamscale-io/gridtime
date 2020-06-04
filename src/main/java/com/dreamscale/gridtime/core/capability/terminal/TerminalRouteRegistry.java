package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageMetaProp;
import com.dreamscale.gridtime.api.terminal.*;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class TerminalRouteRegistry {

    @Autowired
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    private GridClock gridClock;

    private MultiValueMap<Command, TerminalRoute> terminalRoutes = DefaultCollections.multiMap();
    private Map<Command, String> manPageDescriptions = DefaultCollections.map();

    public void registerManPageDescription(Command command, String description) {
        manPageDescriptions.put(command, description);
    }

    public void register(Command command, TerminalRoute terminalRoute) {
        log.info("[TerminalRouteRegistry] Register "+command.name());

        terminalRoutes.add(command, terminalRoute);
    }

    public TalkMessageDto routeCommand(UUID organizationId, UUID memberId, RunCommandInputDto runCommandInputDto) {

        List<TerminalRoute> terminalRoutes = this.terminalRoutes.get(runCommandInputDto.getCommand());

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        if (terminalRoutes != null) {
            for (TerminalRoute route : terminalRoutes) {

                if (route.matches(runCommandInputDto.getArgs())) {
                    Object result = route.route(runCommandInputDto.getArgs());

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

        MemberDetailsEntity memberDetails = memberDetailsRetriever.lookupMemberDetails(memberId);

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

    public CommandManualDto getManual(UUID organizationId, UUID memberId) {

        CommandManualDto manualDto = new CommandManualDto();

        Set<Command> commands = terminalRoutes.keySet();

        for (Command command : commands) {

            String description = manPageDescriptions.get(command);
            List<TerminalRoute> commandPageRoutes = terminalRoutes.get(command);

            CommandManualPageDto manPage = toManPage(command, description, commandPageRoutes);

            manualDto.addPage(manPage);
        }

        return manualDto;
    }

    private CommandManualPageDto toManPage(Command command, String description, List<TerminalRoute> commandPageRoutes) {

        CommandManualPageDto page = new CommandManualPageDto();

        page.setCommand(command);
        page.setDescription(description);

        if (commandPageRoutes != null) {
            for (TerminalRoute route : commandPageRoutes) {
                TerminalRouteDto routeDto = new TerminalRouteDto();

                routeDto.setArgsTemplate(route.getArgsTemplate());
                routeDto.setOptionsHelp(route.getOptionsHelpDescriptions());

                page.addRoute(routeDto);
            }
        }

        return page;
    }

    public CommandManualPageDto getManualPage(UUID organizationId, UUID memberId, Command command) {

        String description = manPageDescriptions.get(command);
        List<TerminalRoute> routes = terminalRoutes.get(command);

        validateAtLeastOneRouteIsFound(command, routes);

        return toManPage(command, description, routes);
    }

    private void validateAtLeastOneRouteIsFound(Command command, List<TerminalRoute> routes) {
        if (routes == null || routes.size() == 0) {

            throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Unable to find any registered terminal routes for command: " + command);

        }
    }

}

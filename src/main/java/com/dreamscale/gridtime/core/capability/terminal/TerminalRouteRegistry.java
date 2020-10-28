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
import java.util.*;

@Slf4j
@Service
public class TerminalRouteRegistry {

    @Autowired
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    private GridClock gridClock;

    private MultiValueMap<Command, TerminalRoute> terminalRoutes = DefaultCollections.multiMap();

    private MultiValueMap<Command, CommandDescriptorDto> commandDescriptorsByCommand = DefaultCollections.multiMap();

    private Map<ActivityContext, CommandManualPageDto> manualPagesByContext = DefaultCollections.map();

    public void register(ActivityContext activityContext, Command command, String description, TerminalRoute ... terminalRoutesForCommand) {
        log.info("[TerminalRouteRegistry] Register "+command.name());

        CommandManualPageDto manPage = findOrCreateManualPage(activityContext);

        CommandDescriptorDto descriptor = createDescriptor(command, description, terminalRoutesForCommand);
        manPage.addCommandDescriptor(descriptor);

        commandDescriptorsByCommand.add(descriptor.getCommand(), descriptor);

        for (TerminalRoute route: terminalRoutesForCommand) {
            terminalRoutes.add(command, route);
        }
    }

    private CommandDescriptorDto createDescriptor(Command command, String description, TerminalRoute[] terminalRoutesForCommand) {

        CommandDescriptorDto descriptor = new CommandDescriptorDto();
        descriptor.setCommand(command);
        descriptor.setDescription(description);

        for (TerminalRoute route : terminalRoutesForCommand) {

            TerminalRouteDto routeDto = new TerminalRouteDto();
            routeDto.setCommand(route.getCommand());
            routeDto.setArgsTemplate(route.getArgsTemplate());
            routeDto.setOptionsHelp(route.getOptionsHelpDescriptions());

            descriptor.addRoute(routeDto);
        }

        return descriptor;
    }

    private CommandManualPageDto findOrCreateManualPage(ActivityContext context) {
        CommandManualPageDto manPage = manualPagesByContext.get(context);

        if (manPage == null) {
            manPage = new CommandManualPageDto(context.name());
            manualPagesByContext.put(context, manPage);
        }
        return manPage;
    }


    public TalkMessageDto routeCommand(UUID organizationId, UUID memberId, CommandInputDto commandInputDto) {

        List<TerminalRoute> terminalRoutes = this.terminalRoutes.get(commandInputDto.getCommand());

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        if (terminalRoutes != null) {
            for (TerminalRoute route : terminalRoutes) {

                if (route.matches(commandInputDto.getCommand(), commandInputDto.getArgs())) {
                    Object result = route.route(commandInputDto.getArgs());

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

        for (ActivityContext context : ActivityContext.values()) {
            CommandManualPageDto groupPage = manualPagesByContext.get(context);
            manualDto.addPage(context, groupPage);
        }

        return manualDto;
    }

    public CommandManualPageDto getManualPage(UUID organizationId, UUID memberId, Command command) {

        CommandManualPageDto manPage = new CommandManualPageDto(command.name());

        List<CommandDescriptorDto> commandDescriptors = commandDescriptorsByCommand.get(command);

        manPage.setCommandDescriptors(commandDescriptors);

        return manPage;
    }

    public CommandManualPageDto getManualPage(UUID organizationId, UUID memberId, ActivityContext group) {

        return manualPagesByContext.get(group);
    }

    private void validateAtLeastOneRouteIsFound(Command command, List<TerminalRoute> routes) {
        if (routes == null || routes.size() == 0) {

            throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Unable to find any registered terminal routes for command: " + command);

        }
    }



}

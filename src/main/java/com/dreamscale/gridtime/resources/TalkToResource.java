package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.operator.WTFCircuitOperator;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TALK_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TalkToResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    WTFCircuitOperator wtfCircuitOperator;

    /**
     * The invoking member joins the specified "-wtf" or "-retro" talk room.
     *
     * The circuit's corresponding "-status" room will automatically be joined.
     *
     * The invoking member will also become a circuit participant.
     *
     * @param roomName
     * @return TalkMessageDto (the notification message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.JOIN_PATH )
    public TalkMessageDto joinRoom(@PathVariable("roomName") String roomName) {

        RequestContext context = RequestContext.get();
        log.info("joinRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.joinRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName);

    }

    /**
     * The invoking member leaves the specified "-wtf" or "-retro" talk room.
     *
     * The circuit's corresponding "-status" room will automatically be left.
     *
     * @param roomName
     * @return TalkMessageDto (the notification message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.LEAVE_PATH )
    public TalkMessageDto leaveRoom(@PathVariable("roomName") String roomName) {

        RequestContext context = RequestContext.get();
        log.info("leaveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.leaveRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName);

    }

    /**
     * Send a chat message to the specified talk room.
     *
     * @param roomName
     * @param chatMessageInputDto
     *
     * @return TalkMessageDto (the chat message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.CHAT_PATH )
    public TalkMessageDto publishChatToRoom(@PathVariable("roomName") String roomName,
                                            @RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("publishChatToRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.publishChatToTalkRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName, chatMessageInputDto.getChatMessage());

    }

    /**
     * Send a text snippet to the specified talk room (used from the IDE plugin).
     *
     * @param roomName
     * @param newSnippetEventDto
     *
     * @return TalkMessageDto (the snippet message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.SNIPPET_PATH )
    public TalkMessageDto publishSnippetToRoom(@PathVariable("roomName") String roomName, @RequestBody NewSnippetEventDto newSnippetEventDto) {

        RequestContext context = RequestContext.get();
        log.info("publishSnippetToRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.publishSnippetToTalkRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName, newSnippetEventDto);

    }

    /**
     * Send a screenshot to the specified talk room
     *
     * @param roomName
     * @param screenshotReferenceInput
     *
     * @return TalkMessageDto (the screenshot message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.SCREENSHOT_PATH )
    public TalkMessageDto publishScreenshotToRoom(@PathVariable("roomName") String roomName, @RequestBody ScreenshotReferenceInputDto screenshotReferenceInput) {

        RequestContext context = RequestContext.get();
        log.info("publishScreenshotToRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.publishScreenshotToTalkRoom(invokingMember.getOrganizationId(), invokingMember.getId(), roomName, screenshotReferenceInput);
    }


    /**
     * Retrieve all the talk messages for a room.
     *
     * //TODO This needs to be a paged interface
     *
     * @param roomName
     *
     * @return List<TalkMessageDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}")
    List<TalkMessageDto> getAllTalkMessagesFromRoom(@PathVariable("roomName") String roomName) {
        RequestContext context = RequestContext.get();
        log.info("getAllTalkMessagesFromRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getAllTalkMessagesFromRoom(invokingMember.getOrganizationId(), invokingMember.getId(), roomName);

    }

    /**
     * Send a chat message to the member's active talk room, useful if sending from an external context
     *
     * @param chatMessageInputDto
     *
     * @return TalkMessageDto (the chat message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.CHAT_PATH )
    public TalkMessageDto publishChatToActiveRoom(@RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("publishChatToActiveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.publishChatToActiveRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), chatMessageInputDto.getChatMessage());

    }

    /**
     * Send a snippet message to the member's active talk room, useful if sending from an external context
     *
     * @param newSnippetEventDto
     *
     * @return TalkMessageDto (the snippet message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.SNIPPET_PATH )
    public TalkMessageDto publishSnippetToActiveRoom(@RequestBody NewSnippetEventDto newSnippetEventDto) {

        RequestContext context = RequestContext.get();
        log.info("publishSnippetToActiveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.publishSnippetToActiveCircuit(invokingMember.getOrganizationId(),
                invokingMember.getId(), newSnippetEventDto);

    }

    /**
     * Send a screenshot message to the member's active talk room, useful if sending from an external context
     *
     * @param screenshotReferenceInput
     *
     * @return TalkMessageDto (the screenshot message sent to the room)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.SCREENSHOT_PATH )
    public TalkMessageDto publishScreenshotToActiveRoom(@RequestBody ScreenshotReferenceInputDto screenshotReferenceInput) {

        RequestContext context = RequestContext.get();
        log.info("publishScreenshotToActiveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.publishScreenshotToActiveRoom(invokingMember.getOrganizationId(), invokingMember.getId(), screenshotReferenceInput);
    }


}

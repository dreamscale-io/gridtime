package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.capability.circuit.DirectMessageOperator;
import com.dreamscale.gridtime.core.capability.circuit.RoomOperator;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TALK_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TalkToResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    RoomOperator roomOperator;

    @Autowired
    DirectMessageOperator directMessageOperator;


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
    public void joinRoom(@PathVariable("roomName") String roomName) {

        RequestContext context = RequestContext.get();
        log.info("joinRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        roomOperator.joinRoom(invokingMember.getOrganizationId(), invokingMember.getId(), roomName);
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
    public void leaveRoom(@PathVariable("roomName") String roomName) {

        RequestContext context = RequestContext.get();
        log.info("leaveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        roomOperator.leaveRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName);

    }

    /**
     * Sends chat message directly to the specified member
     *
     * The invoking member will also become a circuit participant.
     *
     * @param memberId
     * @return TalkMessageDto (the message sent to the member)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.MEMBER_PATH + "/{memberId}" + ResourcePaths.CHAT_PATH )
    public TalkMessageDto sendDirectChatMessage(@PathVariable("memberId") String memberId, @RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("sendDirectChatMessage, user={}", context.getRootAccountId());

        UUID memberIdParsed = UUID.fromString(memberId);


        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return directMessageOperator.sendDirectChatMessage(invokingMember.getOrganizationId(), invokingMember.getId(), memberIdParsed, chatMessageInputDto.getChatMessage());
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

        return roomOperator.publishChatToTalkRoom(invokingMember.getOrganizationId(),
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

        return roomOperator.publishSnippetToTalkRoom(invokingMember.getOrganizationId(),
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

        return roomOperator.publishScreenshotToTalkRoom(invokingMember.getOrganizationId(), invokingMember.getId(), roomName, screenshotReferenceInput);
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

        return roomOperator.getAllTalkMessagesFromRoom(invokingMember.getOrganizationId(), invokingMember.getId(), roomName);

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

        return roomOperator.publishChatToActiveRoom(invokingMember.getOrganizationId(),
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

        return roomOperator.publishSnippetToActiveRoom(invokingMember.getOrganizationId(),
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

        return roomOperator.publishScreenshotToActiveRoom(invokingMember.getOrganizationId(), invokingMember.getId(), screenshotReferenceInput);
    }


}

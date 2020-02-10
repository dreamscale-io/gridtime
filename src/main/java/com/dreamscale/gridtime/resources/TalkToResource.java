package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.LearningCircuitOperator;
import com.dreamscale.gridtime.core.service.OrganizationService;
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
    OrganizationService organizationService;

    @Autowired
    LearningCircuitOperator learningCircuitOperator;


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.CHAT_PATH )
    public TalkMessageDto publishChatToRoom(@PathVariable("roomName") String roomName,
                                            @RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("publishChatToRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.publishChatToTalkRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName, chatMessageInputDto.getChatMessage());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.SNIPPET_PATH )
    public TalkMessageDto publishSnippetToRoom(@PathVariable("roomName") String roomName, @RequestBody NewSnippetEventDto newSnippetEventDto) {

        RequestContext context = RequestContext.get();
        log.info("publishSnippetToRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.publishSnippetToTalkRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), roomName, newSnippetEventDto);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.SCREENSHOT_PATH )
    public TalkMessageDto publishScreenshotToRoom(@PathVariable("roomName") String talkRoomId, @RequestBody ScreenshotReferenceInputDto screenshotReferenceInput) {

        RequestContext context = RequestContext.get();
        log.info("publishScreenshotToRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.publishScreenshotToTalkRoom(invokingMember.getOrganizationId(), invokingMember.getId(), talkRoomId, screenshotReferenceInput);
    }

    @GetMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + "/{roomName}")
    List<TalkMessageDto> getAllTalkMessagesFromRoom(@PathVariable("roomName") String talkRoomId) {
        RequestContext context = RequestContext.get();
        log.info("getAllTalkMessagesFromRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.getAllTalkMessagesFromRoom(invokingMember.getOrganizationId(), invokingMember.getId(), talkRoomId);

    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.CHAT_PATH )
    public TalkMessageDto publishChatToActiveRoom(@RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("publishChatToActiveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.publishChatToActiveRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), chatMessageInputDto.getChatMessage());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.SNIPPET_PATH )
    public TalkMessageDto publishSnippetToActiveRoom(@RequestBody NewSnippetEventDto newSnippetEventDto) {

        RequestContext context = RequestContext.get();
        log.info("publishSnippetToActiveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.publishSnippetToActiveCircuit(invokingMember.getOrganizationId(),
                invokingMember.getId(), newSnippetEventDto);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.SCREENSHOT_PATH )
    public TalkMessageDto publishScreenshotToActiveRoom(@RequestBody ScreenshotReferenceInputDto screenshotReferenceInput) {

        RequestContext context = RequestContext.get();
        log.info("publishScreenshotToActiveRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.publishScreenshotToActiveRoom(invokingMember.getOrganizationId(), invokingMember.getId(), screenshotReferenceInput);
    }


}

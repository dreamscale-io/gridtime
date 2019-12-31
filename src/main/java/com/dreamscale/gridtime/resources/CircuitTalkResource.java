package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.event.NewSnippetEvent;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.LearningCircuitService;
import com.dreamscale.gridtime.core.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TALK_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class CircuitTalkResource {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    LearningCircuitService learningCircuitService;


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ROOM_PATH + "/{talkRoomId}" + ResourcePaths.CHAT_PATH )
    public TalkMessageDto publishChatToRoomFeed(@PathVariable("talkRoomId") String talkRoomId,
                                                @RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("publishChatToRoomFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.publishChatToTalkRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), talkRoomId, chatMessageInputDto.getChatMessage());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.ROOM_PATH + "/{talkRoomId}" + ResourcePaths.SNIPPET_PATH )
    public TalkMessageDto publishSnippetToRoomFeed(@PathVariable("talkRoomId") String talkRoomId, @RequestBody NewSnippetEvent newSnippetEvent) {

        RequestContext context = RequestContext.get();
        log.info("publishSnippetToRoomFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.publishSnippetToTalkRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), talkRoomId, newSnippetEvent);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ROOM_PATH + "/{talkRoomId}" + ResourcePaths.SCREENSHOT_PATH )
    public TalkMessageDto publishScreenshotToRoomFeed(@PathVariable("talkRoomId") String talkRoomId, @RequestBody ScreenshotReferenceInputDto screenshotReferenceInput) {

        RequestContext context = RequestContext.get();
        log.info("publishScreenshotToRoomFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.publishScreenshotToTalkRoom(invokingMember.getOrganizationId(), invokingMember.getId(), talkRoomId, screenshotReferenceInput);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.CHAT_PATH )
    public TalkMessageDto publishChatToActiveRoom(@RequestBody ChatMessageInputDto chatMessageInputDto) {

        RequestContext context = RequestContext.get();
        log.info("publishChatToActiveRoom, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.publishChatToActiveRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), chatMessageInputDto.getChatMessage());

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.SNIPPET_PATH )
    public TalkMessageDto publishSnippetToActiveRoom(@RequestBody NewSnippetEvent newSnippetEvent) {

        RequestContext context = RequestContext.get();
        log.info("publishSnippetToActiveRoom, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.publishSnippetToActiveRoom(invokingMember.getOrganizationId(),
                invokingMember.getId(), newSnippetEvent);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ROOM_PATH + ResourcePaths.ACTIVE_PATH + ResourcePaths.SCREENSHOT_PATH )
    public TalkMessageDto publishScreenshotToActiveRoom(@RequestBody ScreenshotReferenceInputDto screenshotReferenceInput) {

        RequestContext context = RequestContext.get();
        log.info("publishScreenshotToActiveRoom, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.publishScreenshotToActiveRoom(invokingMember.getOrganizationId(), invokingMember.getId(), screenshotReferenceInput);
    }




}

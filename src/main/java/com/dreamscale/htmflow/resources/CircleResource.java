package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.CircleService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.ActiveStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.CIRCLE_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class CircleResource {


    @Autowired
    OrganizationService organizationService;

    @Autowired
    CircleService circleService;

    /**
     * Creates a new adhoc circle for troubleshooting the WTF, and "pulls the andon cord" for the team member,
     * updating the work status, for all team members to see.  The team member, will automatically be added
     * to the circle
     * @param circleSessionInputDto CreateWTFCircleInputDto
     * @return CircleDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH)
    public CircleDto createNewWTFCircle(@RequestBody CreateWTFCircleInputDto circleSessionInputDto) {
        RequestContext context = RequestContext.get();
        log.info("createNewWTFCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        CircleDto circleDto = circleService.createNewAdhocCircle(memberEntity.getOrganizationId(), memberEntity.getId(), circleSessionInputDto.getProblemDescription());

        return circleDto;

    }

    /**
     * Closes an existing circle, and resolves with YAY!
     * @param circleInputDto CircleInputDto
     * @return CircleDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + ResourcePaths.RESOLVE_PATH)
    public void closeWTFCircle(@RequestBody CircleInputDto circleInputDto) {
        RequestContext context = RequestContext.get();
        log.info("closeWTFCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        circleService.closeCircle(memberEntity.getOrganizationId(), memberEntity.getId(), circleInputDto.getCircleId());

    }

    /**
     * Posts a new chat message to the circle's feed
     * @param chatMessageInputDto CreateWTFCircleInputDto
     * @return CircleDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + ResourcePaths.CHAT_PATH)
    public FeedMessageDto postChatMessageToCircleFeed(@RequestBody ChatMessageInputDto chatMessageInputDto) {
        RequestContext context = RequestContext.get();
        log.info("postChatMessageToCircleFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postChatMessageToCircleFeed(memberEntity.getOrganizationId(), memberEntity.getId(), chatMessageInputDto);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + ResourcePaths.SNIPPET_PATH)
    public void saveFlowSnippet(@RequestBody NewSnippetEvent snippet) {
        RequestContext context = RequestContext.get();
        log.info("saveFlowSnippet, user={}, snippet={}", context.getMasterAccountId(), snippet);

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        circleService.saveSnippetEvent(memberEntity.getOrganizationId(), memberEntity.getId(),  snippet);
    }

    /**
     * Retrieve all the messages posted to the circle's feed
     * @return List<FeedMessageDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WTF_PATH + ResourcePaths.FEED_PATH)
    public List<FeedMessageDto> getAllMessagesForCircleFeed(@RequestParam("circle_id") String circleId) {
        RequestContext context = RequestContext.get();
        log.info("getAllMessagesForCircleFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getAllMessagesForCircleFeed(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(circleId));

    }

}

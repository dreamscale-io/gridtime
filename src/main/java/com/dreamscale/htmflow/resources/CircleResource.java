package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.CircleService;
import com.dreamscale.htmflow.core.service.OrganizationService;
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
     * Creates a new adhoc circle for troubleshooting a WTF, and "pulls the andon cord" for the team member,
     * updating the work status, for all team members to see.  The team member, will automatically be added
     * to the circle
     * @param circleSessionInputDto CreateWTFCircleInputDto
     * @return CircleDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping()
    public CircleDto createNewWTFCircle(@RequestBody CreateWTFCircleInputDto circleSessionInputDto) {
        RequestContext context = RequestContext.get();
        log.info("createNewWTFCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.createNewAdhocCircle(memberEntity.getOrganizationId(), memberEntity.getId(), circleSessionInputDto.getProblemDescription());

    }

    /**
     * Retrieves all open circles across all users in the organization
     * @return List<CircleDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public List<CircleDto> getAllOpenCircles() {
        RequestContext context = RequestContext.get();
        log.info("createNewWTFCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getAllOpenCircles(memberEntity.getOrganizationId(), memberEntity.getId());
    }

    /**
     * Closes an existing circle, and resolves with YAY!
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}"  + ResourcePaths.TRANSITION_PATH + ResourcePaths.CLOSE_PATH)
    public void closeWTFCircle(@PathVariable("id") String circleId) {
        RequestContext context = RequestContext.get();
        log.info("closeWTFCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        circleService.closeCircle(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(circleId));

    }

    /**
     * Posts a new chat message to the circle's feed
     * @param chatMessageInputDto ChatMessageInputDto
     * @return FeedMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}"  + ResourcePaths.FEED_PATH + ResourcePaths.CHAT_PATH)
    public FeedMessageDto postChatMessageToCircleFeed(@PathVariable("id") String circleId, @RequestBody ChatMessageInputDto chatMessageInputDto) {
        RequestContext context = RequestContext.get();
        log.info("postChatMessageToCircleFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postChatMessageToCircleFeed(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(circleId), chatMessageInputDto.getChatMessage());

    }

    /**
     * Posts a new screenshot file reference to the circle's feed
     * @param screenshotReferenceInputDto ScreenshotReferenceInputDto
     * @return FeedMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}"  + ResourcePaths.FEED_PATH + ResourcePaths.SCREENSHOT_PATH)
    public FeedMessageDto postScreenshotReferenceToCircleFeed(@PathVariable("id") String circleId, @RequestBody ScreenshotReferenceInputDto screenshotReferenceInputDto) {
        RequestContext context = RequestContext.get();
        log.info("postChatMessageToCircleFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postScreenshotReferenceToCircleFeed(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(circleId), screenshotReferenceInputDto);

    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ACTIVE_PATH + ResourcePaths.FEED_PATH + ResourcePaths.SNIPPET_PATH)
    public FeedMessageDto postSnippetToActiveCircleFeed(@RequestBody NewSnippetEvent snippet) {
        RequestContext context = RequestContext.get();
        log.info("saveFlowSnippet, user={}, snippet={}", context.getMasterAccountId(), snippet);

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postSnippetToActiveCircleFeed(memberEntity.getOrganizationId(), memberEntity.getId(),  snippet);
    }

    /**
     * Retrieve all the messages posted to the circle's feed
     * @return List<FeedMessageDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}"  + ResourcePaths.FEED_PATH)
    public List<FeedMessageDto> getAllMessagesForCircleFeed(@PathVariable("id") String circleId) {
        RequestContext context = RequestContext.get();
        log.info("getAllMessagesForCircleFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getAllMessagesForCircleFeed(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(circleId));

    }

}

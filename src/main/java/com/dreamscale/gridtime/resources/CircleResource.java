package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circle.*;
import com.dreamscale.gridtime.api.event.NewSnippetEvent;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.CircleService;
import com.dreamscale.gridtime.core.service.OrganizationService;
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

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.createNewAdhocCircle(invokingMember.getOrganizationId(), invokingMember.getId(), circleSessionInputDto.getProblemDescription());

    }

    /**
     * Retrieves the active circle for the user
     * @return List<CircleDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ACTIVE_PATH)
    public CircleDto getActiveCircle() {
        RequestContext context = RequestContext.get();
        log.info("getActiveCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getActiveCircle(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves all open circles across all users in the organization
     * @return List<CircleDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public List<CircleDto> getAllOpenCircles() {
        RequestContext context = RequestContext.get();
        log.info("getAllOpenCircles, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getAllOpenCircles(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves all circles on the members do it later list
     * @return List<CircleDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.DO_IT_LATER_PATH)
    public List<CircleDto> getAllDoItLaterCircles() {
        RequestContext context = RequestContext.get();
        log.info("getAllDoItLaterCircles, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getAllDoItLaterCircles(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Closes an existing circle, and resolves the member status with YAY!
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}"  + ResourcePaths.TRANSITION_PATH + ResourcePaths.CLOSE_PATH)
    public CircleDto closeWTFCircle(@PathVariable("id") String circleId) {
        RequestContext context = RequestContext.get();
        log.info("closeWTFCircle, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.closeCircle(invokingMember.getOrganizationId(), invokingMember.getId(), UUID.fromString(circleId));
    }

    /**
     * Shelves an existing circle, by adding to the do it later queue
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}"  + ResourcePaths.TRANSITION_PATH + ResourcePaths.DO_IT_LATER_PATH)
    public CircleDto shelveCircleWithDoItLater(@PathVariable("id") String circleId) {
        RequestContext context = RequestContext.get();
        log.info("shelveCircleWithDoItLater, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.shelveCircleWithDoItLater(invokingMember.getOrganizationId(), invokingMember.getId(), UUID.fromString(circleId));

    }

    /**
     * Resume an existing shelved circle on the do it later queue
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}"  + ResourcePaths.TRANSITION_PATH + ResourcePaths.RESUME_PATH)
    public CircleDto resumeAnExistingCircleFromDoItLaterShelf(@PathVariable("id") String circleId) {
        RequestContext context = RequestContext.get();
        log.info("resumeAnExistingCircleOnDoItLaterShelf, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.resumeAnExistingCircleFromDoItLaterShelf(invokingMember.getOrganizationId(), invokingMember.getId(), UUID.fromString(circleId));
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

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postChatMessageToCircleFeed(invokingMember.getOrganizationId(), invokingMember.getId(), UUID.fromString(circleId), chatMessageInputDto.getChatMessage());

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
        log.info("postScreenshotReferenceToCircleFeed, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postScreenshotReferenceToCircleFeed(invokingMember.getOrganizationId(), invokingMember.getId(), UUID.fromString(circleId), screenshotReferenceInputDto);

    }


    /**
     * Posts a code snippet to the active circle feed for the member, intended to be used from the IDE plugin
     * where the context of the active circle is not known
     * @param snippet NewSnippetEvent
     * @return FeedMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ACTIVE_PATH + ResourcePaths.FEED_PATH + ResourcePaths.SNIPPET_PATH)
    public FeedMessageDto postSnippetToActiveCircleFeed(@RequestBody NewSnippetEvent snippet) {
        RequestContext context = RequestContext.get();
        log.info("postSnippetToActiveCircleFeed, user={}, snippet={}", context.getMasterAccountId(), snippet);

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.postSnippetToActiveCircleFeed(invokingMember.getOrganizationId(), invokingMember.getId(),  snippet);
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

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return circleService.getAllMessagesForCircleFeed(invokingMember.getOrganizationId(), invokingMember.getId(), UUID.fromString(circleId));

    }

}

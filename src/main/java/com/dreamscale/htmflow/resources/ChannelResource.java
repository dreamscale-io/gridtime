package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.ActiveUserContextDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.channel.ChannelMessageDto;
import com.dreamscale.htmflow.api.channel.ChatMessageInputDto;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.ActiveUserContextService;
import com.dreamscale.htmflow.core.service.CircleService;
import com.dreamscale.htmflow.core.service.RealtimeChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.CHANNEL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ChannelResource {

    @Autowired
    ActiveUserContextService activeUserContextService;

    @Autowired
    CircleService circleService;

    @Autowired
    RealtimeChannelService realtimeChannelService;

    /**
     * Posts a chat message to the specified realtime channel
     * @param chatMessageInputDto ChatMessageInputDto
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.MESSAGE_PATH)
    public ChannelMessageDto postChatMessageToChannel(@PathVariable("id") String channelIdStr, @RequestBody ChatMessageInputDto chatMessageInputDto) {
        RequestContext context = RequestContext.get();
        log.info("postChatMessageToChannel, user={}", context.getMasterAccountId());

        ActiveUserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);
        return realtimeChannelService.postMessageToChannel(channelId, activeUser.getOrganizationId(), activeUser.getMemberId(), chatMessageInputDto);

    }

    /**
     * Invoking member joins the channel, which causes the member to receive all updates via Realtime server
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.JOIN_PATH)
    public SimpleStatusDto joinChannel(@PathVariable("id") String channelIdStr) {
        RequestContext context = RequestContext.get();
        log.info("joinChannel, user={}", context.getMasterAccountId());

        ActiveUserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeChannelService.joinChannel(channelId, activeUser);

    }

    /**
     * Invoking member leaves the channel, which causes the member to stop receiving all updates via Realtime server
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.LEAVE_PATH)
    public SimpleStatusDto leaveChannel(@PathVariable("id") String channelIdStr) {
        RequestContext context = RequestContext.get();
        log.info("leaveChannel, user={}", context.getMasterAccountId());

        ActiveUserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeChannelService.leaveChannel(channelId, activeUser);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}" + ResourcePaths.MEMBER_PATH )
    public List<ActiveUserContextDto> getActiveChannelMembers(@PathVariable("id") String channelIdStr) {
        RequestContext context = RequestContext.get();
        log.info("listActiveMembers, user={}", context.getMasterAccountId());

        ActiveUserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeChannelService.getActiveChannelMembers(channelId, activeUser);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}" + ResourcePaths.MESSAGE_PATH )
    public List<ChannelMessageDto> getAllChannelMessages(@PathVariable("id") String channelIdStr) {
        RequestContext context = RequestContext.get();
        log.info("listActiveMembers, user={}", context.getMasterAccountId());

        ActiveUserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeChannelService.getAllChannelMessages(channelId, activeUser);

    }

}

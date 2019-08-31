package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.UserContextDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.network.ChannelMessageDto;
import com.dreamscale.gridtime.api.network.ChatMessageInputDto;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.ActiveUserContextService;
import com.dreamscale.gridtime.core.service.CircleService;
import com.dreamscale.gridtime.core.service.RealtimeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.NETWORK_PATH + ResourcePaths.CHANNEL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class NetworkChannelResource {

    @Autowired
    ActiveUserContextService activeUserContextService;

    @Autowired
    CircleService circleService;

    @Autowired
    RealtimeNetworkService realtimeNetworkService;

    /**
     * Posts a chat message to the specified realtime channel
     * @param chatMessageInputDto ChatMessageInputDto
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.EMIT_PATH)
    public ChannelMessageDto postChatMessageToChannel(@PathVariable("id") String channelIdStr, @RequestBody ChatMessageInputDto chatMessageInputDto) {
        RequestContext context = RequestContext.get();
        log.info("postChatMessageToChannel, user={}", context.getMasterAccountId());

        UserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);
        return realtimeNetworkService.postMessageToChannel(channelId, activeUser.getOrganizationId(), activeUser.getMemberId(), chatMessageInputDto);

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

        UserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeNetworkService.joinChannel(channelId, activeUser);

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

        UserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeNetworkService.leaveChannel(channelId, activeUser);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}" + ResourcePaths.MEMBER_PATH )
    public List<UserContextDto> getActiveChannelMembers(@PathVariable("id") String channelIdStr) {
        RequestContext context = RequestContext.get();
        log.info("listActiveMembers, user={}", context.getMasterAccountId());

        UserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeNetworkService.getActiveChannelMembers(channelId, activeUser);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}" + ResourcePaths.MESSAGE_PATH  )
    public List<ChannelMessageDto> getAllChannelMessages(@PathVariable("id") String channelIdStr) {
        RequestContext context = RequestContext.get();
        log.info("listActiveMembers, user={}", context.getMasterAccountId());

        UserContextDto activeUser = activeUserContextService.getActiveUserContext(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);

        return realtimeNetworkService.getAllChannelMessages(channelId, activeUser);

    }

}

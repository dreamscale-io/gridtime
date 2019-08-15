package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.channel.ChatMessageInputDto;
import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.CircleService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.RealtimeChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.CHANNEL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ChannelResource {


    @Autowired
    OrganizationService organizationService;

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
    public SimpleStatusDto postChatMessageToChannel(@PathVariable("id") String channelIdStr, @RequestBody ChatMessageInputDto chatMessageInputDto) {
        RequestContext context = RequestContext.get();
        log.info("postChatMessageToChannel, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());
        UUID channelId = UUID.fromString(channelIdStr);
        return realtimeChannelService.postMessageToChannel(channelId, invokingMember.getOrganizationId(), invokingMember.getId(), chatMessageInputDto);

    }



}

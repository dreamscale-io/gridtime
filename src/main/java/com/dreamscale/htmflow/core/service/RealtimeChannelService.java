package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.channel.ChatMessageInputDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.channel.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.gridtime.machine.commons.JSONTransformer;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class RealtimeChannelService {

    @Autowired
    RealtimeChannelRepository realtimeChannelRepository;

    @Autowired
    RealtimeChannelMessageRepository realtimeChannelMessageRepository;

    @Autowired
    TimeService timeService;

    public UUID createChannel(UUID organizationId, UUID ownerId, ChannelType channelType) {
        RealtimeChannelEntity entity = new RealtimeChannelEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizationId(organizationId);
        entity.setOwnerId(ownerId);
        entity.setChannelType(channelType);

        realtimeChannelRepository.save(entity);

        return entity.getId();
    }


    public SimpleStatusDto postMessageToChannel(UUID channelId, UUID organizationId, UUID memberId, ChatMessageInputDto jsonMessage) {

        RealtimeChannelEntity channelEntity = realtimeChannelRepository.findOne(channelId);

        validatePermissionsAndChannelExists(channelEntity, organizationId, memberId);

        RealtimeChannelMessageEntity messageEntity = new RealtimeChannelMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setMessageType(MessageType.CHAT);
        messageEntity.setChannelId(channelId);
        messageEntity.setFromMemberId(memberId);
        messageEntity.setJson(JSONTransformer.toJson(jsonMessage));
        messageEntity.setMessageTime(timeService.now());

        realtimeChannelMessageRepository.save(messageEntity);

        return new SimpleStatusDto(Status.SENT, "Message sent to channel");
    }

    private void validatePermissionsAndChannelExists(RealtimeChannelEntity channelEntity, UUID organizationId, UUID memberId) {
        if (channelEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.CHANNEL_NOT_FOUND, "Channel not found");
        } else {

            if (!channelEntity.getOrganizationId().equals(organizationId)) {
                throw new BadRequestException(ValidationErrorCodes.CHANNEL_ACCESS_DENIED, "Member unabled to write to channel");
            }
        }

    }
}

package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.account.ActiveUserContextDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.channel.ChatMessageInputDto;
import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.api.circle.CircleMemberDto;
import com.dreamscale.htmflow.api.circle.FeedMessageDto;
import com.dreamscale.htmflow.api.journal.JournalEntryDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.channel.*;
import com.dreamscale.htmflow.core.domain.circle.CircleEntity;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.domain.circle.CircleMemberEntity;
import com.dreamscale.htmflow.core.domain.circle.CircleMessageEntity;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.gridtime.machine.commons.JSONTransformer;
import com.dreamscale.htmflow.core.hooks.realtime.RealtimeConnectionFactory;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RealtimeChannelService {

    @Autowired
    RealtimeChannelRepository realtimeChannelRepository;

    @Autowired
    RealtimeChannelMessageRepository realtimeChannelMessageRepository;

    @Autowired
    RealtimeChannelMemberRepository realtimeChannelMemberRepository;

    @Autowired
    RealtimeConnectionFactory realtimeConnectionFactory;

    @Autowired
    TimeService timeService;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<ActiveUserContextDto, RealtimeChannelMemberEntity> channelMemberMapper;

    @PostConstruct
    private void init() {
        channelMemberMapper = mapperFactory.createDtoEntityMapper(ActiveUserContextDto.class, RealtimeChannelMemberEntity.class);
    }


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

    public List<ActiveUserContextDto> getActiveChannelMembers(UUID channelId, ActiveUserContextDto activeUser) {

        RealtimeChannelMemberEntity memberInChannel = realtimeChannelMemberRepository.findByChannelIdAndMemberId(channelId, activeUser.getMemberId());

        if (memberInChannel == null) {
            throw new BadRequestException(ValidationErrorCodes.CHANNEL_ACCESS_DENIED, "Cant list members if member not in channel");
        }

        List<RealtimeChannelMemberEntity> channelMembers = realtimeChannelMemberRepository.findByChannelId(channelId);

        return channelMemberMapper.toApiList(channelMembers);
    }

    public SimpleStatusDto joinChannel(UUID channelId, ActiveUserContextDto activeUser) {

        RealtimeChannelMemberEntity channelMembership = realtimeChannelMemberRepository.findByChannelIdAndMemberId(channelId, activeUser.getMemberId());

        if (channelMembership != null) {
            return new SimpleStatusDto(Status.NO_ACTION, "No action, member already in channel");
        } else {

            channelMembership = new RealtimeChannelMemberEntity();
            channelMembership.setId(UUID.randomUUID());
            channelMembership.setJoinTime(timeService.now());
            channelMembership.setChannelId(channelId);
            channelMembership.setOrganizationId(activeUser.getOrganizationId());
            channelMembership.setMemberId(activeUser.getMemberId());
            channelMembership.setTeamId(activeUser.getTeamId());

            realtimeChannelMemberRepository.save(channelMembership);
        }

        return new SimpleStatusDto(Status.VALID, "Joined channel");
    }

    public SimpleStatusDto leaveChannel(UUID channelId, ActiveUserContextDto activeUser) {

        RealtimeChannelMemberEntity channelMembership = realtimeChannelMemberRepository.findByChannelIdAndMemberId(channelId, activeUser.getMemberId());

        if (channelMembership != null) {
            realtimeChannelMemberRepository.delete(channelMembership);
        } else {
            return new SimpleStatusDto(Status.NO_ACTION, "No action, member not found in channel");
        }

        return new SimpleStatusDto(Status.VALID, "Left channel");
    }
}

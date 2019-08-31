package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.account.UserContextDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.network.ChannelMessageDto;
import com.dreamscale.gridtime.api.network.ChatMessageInputDto;
import com.dreamscale.gridtime.api.network.MemberChannelsDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.domain.channel.*;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.hooks.realtime.RealtimeConnectionFactory;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RealtimeNetworkService {

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

    private DtoEntityMapper<UserContextDto, RealtimeChannelMemberEntity> channelMemberMapper;
    private DtoEntityMapper<ChannelMessageDto, RealtimeChannelMessageEntity> channelMessageMapper;


    @PostConstruct
    private void init() {
        channelMemberMapper = mapperFactory.createDtoEntityMapper(UserContextDto.class, RealtimeChannelMemberEntity.class);
        channelMessageMapper = mapperFactory.createDtoEntityMapper(ChannelMessageDto.class, RealtimeChannelMessageEntity.class);
    }

    public MemberChannelsDto getAllMemberChannels(UserContextDto userContext) {

        MemberChannelsDto memberChannelsDto = new MemberChannelsDto();

        memberChannelsDto.setUserContext(userContext);

        List<RealtimeChannelMemberEntity> channels = realtimeChannelMemberRepository.findByMemberId(userContext.getMemberId());

        List<UUID> channelIds = extractChannelIds(channels);

        memberChannelsDto.setListeningToChannels(channelIds);

        return memberChannelsDto;
    }

    private List<UUID> extractChannelIds(List<RealtimeChannelMemberEntity> channels) {
        List<UUID> channelIds = new ArrayList<>();

        for (RealtimeChannelMemberEntity channelJoined : channels) {
                channelIds.add(channelJoined.getChannelId());
        }

        return channelIds;
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


    public ChannelMessageDto postMessageToChannel(UUID channelId, UUID organizationId, UUID memberId, ChatMessageInputDto jsonMessage) {

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

        return channelMessageMapper.toApi(messageEntity);
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

    public List<ChannelMessageDto> getAllChannelMessages(UUID channelId, UserContextDto activeUser) {

        RealtimeChannelMemberEntity memberInChannel = realtimeChannelMemberRepository.findByChannelIdAndMemberId(channelId, activeUser.getMemberId());

        if (memberInChannel == null) {
            throw new BadRequestException(ValidationErrorCodes.CHANNEL_ACCESS_DENIED, "Cant list messages if member not in channel");
        }

        List<RealtimeChannelMessageEntity> messages = realtimeChannelMessageRepository.findByChannelIdOrderByMessageTime(channelId);

        return channelMessageMapper.toApiList(messages);

    }

    public List<UserContextDto> getActiveChannelMembers(UUID channelId, UserContextDto activeUser) {

        RealtimeChannelMemberEntity memberInChannel = realtimeChannelMemberRepository.findByChannelIdAndMemberId(channelId, activeUser.getMemberId());

        if (memberInChannel == null) {
            throw new BadRequestException(ValidationErrorCodes.CHANNEL_ACCESS_DENIED, "Cant list members if member not in channel");
        }

        List<RealtimeChannelMemberEntity> channelMembers = realtimeChannelMemberRepository.findByChannelId(channelId);

        return channelMemberMapper.toApiList(channelMembers);
    }

    public SimpleStatusDto joinChannel(UUID channelId, UserContextDto activeUser) {

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

    public SimpleStatusDto leaveChannel(UUID channelId, UserContextDto activeUser) {

        RealtimeChannelMemberEntity channelMembership = realtimeChannelMemberRepository.findByChannelIdAndMemberId(channelId, activeUser.getMemberId());

        if (channelMembership != null) {
            realtimeChannelMemberRepository.delete(channelMembership);
        } else {
            return new SimpleStatusDto(Status.NO_ACTION, "No action, member not found in channel");
        }

        return new SimpleStatusDto(Status.VALID, "Left channel");
    }



}

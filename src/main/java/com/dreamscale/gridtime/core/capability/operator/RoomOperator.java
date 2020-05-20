package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.GridClock;
import com.dreamscale.gridtime.core.service.MemberDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RoomOperator {

    public static final String ROOM_URN_PREFIX = "/talk/to/room/";


    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    private TalkRoomMessageRepository talkRoomMessageRepository;

    @Transactional
    public void joinRoom(UUID organizationId, UUID memberId, String roomName) {

        TalkRoomEntity roomEntity = lookupAndValidateRoom(organizationId, roomName);

        joinTheGridRoomAndTalkRoom(organizationId, memberId, roomEntity);

    }

    public TalkMessageDto publishChatToTalkRoom(UUID organizationId, UUID fromMemberId, String talkRoomName, String chatMessage) {

        TalkRoomEntity roomEntity = lookupAndValidateRoom(organizationId, talkRoomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();
        UUID messageId = UUID.randomUUID();

        String urn = ROOM_URN_PREFIX + talkRoomName;

        return sendRoomMessage(urn, messageId, now, nanoTime, fromMemberId, roomEntity.getId(), chatMessage);
    }

    private TalkRoomEntity lookupAndValidateRoom(UUID organizationId, String talkRoomName) {
        TalkRoomEntity roomEntity = null;

        if (isUUID(talkRoomName)) {
            roomEntity = talkRoomRepository.findByOrganizationIdAndId(organizationId, UUID.fromString(talkRoomName));
        } else {
            roomEntity = talkRoomRepository.findByOrganizationIdAndRoomName(organizationId, talkRoomName);
        }

        validateRoomIsFound(roomEntity, talkRoomName);

        return roomEntity;
    }

    private boolean isUUID(String talkRoomName) {
        return (talkRoomName.length() == 36);
    }

    private TalkMessageDto sendRoomMessage(String urn, UUID messageId, LocalDateTime now, Long nanoTime, UUID fromMemberId, UUID roomId, String chatMessage) {

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.CHAT);
        messageEntity.setJsonBody(JSONTransformer.toJson(new ChatMessageDetailsDto(chatMessage)));

        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        return talkMessageDto;
    }

    private TalkMessageDto toTalkMessageDto(String urn, TalkRoomMessageEntity messageEntity) {

        TalkMessageDto messageDto = new TalkMessageDto();
        messageDto.setId(messageEntity.getId());
        messageDto.setUrn(urn);
        messageDto.setUri(messageEntity.getToRoomId().toString());
        messageDto.setRequest(getRequestUriFromContext());
        messageDto.addMetaProp(TalkMessageMetaProp.FROM_MEMBER_ID, messageEntity.getFromId().toString());

        MemberDetailsEntity memberDetails = memberDetailsService.lookupMemberDetails(messageEntity.getFromId());

        if (memberDetails != null) {
            messageDto.addMetaProp(TalkMessageMetaProp.FROM_USERNAME, memberDetails.getUsername());
            messageDto.addMetaProp(TalkMessageMetaProp.FROM_FULLNAME, memberDetails.getFullName());
        }

        messageDto.setMessageTime(messageEntity.getPosition());
        messageDto.setNanoTime(messageEntity.getNanoTime());
        messageDto.setMessageType(messageEntity.getMessageType().getSimpleClassName());
        messageDto.setData(JSONTransformer.fromJson(messageEntity.getJsonBody(), messageEntity.getMessageType().getMessageClazz()));

        return messageDto;
    }

    private String getRequestUriFromContext() {

        RequestContext context = RequestContext.get();

        if (context != null) {
            return context.getRequestUri();
        } else {
            return null;
        }
    }


    private void joinTheGridRoomAndTalkRoom(UUID organizationId, UUID memberId, TalkRoomEntity roomEntity) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        String roomName = roomEntity.getRoomName();

        log.debug("[RoomOperator] Member {} joining room {} at {}", memberId, roomName, nanoTime);

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity == null) {
            roomMemberEntity = new TalkRoomMemberEntity();
            roomMemberEntity.setId(UUID.randomUUID());
            roomMemberEntity.setRoomId(roomEntity.getId());
            roomMemberEntity.setOrganizationId(organizationId);
            roomMemberEntity.setMemberId(memberId);
            roomMemberEntity.setJoinTime(now);

            talkRoomMemberRepository.save(roomMemberEntity);

        } else {
            log.warn("[RoomOperator] Member {} already joined {}", memberId, roomName);
        }

        talkRouter.joinRoom(organizationId, memberId, roomEntity.getId());
    }

    @Transactional
    public void leaveRoom(UUID organizationId, UUID memberId, String roomName) {

        TalkRoomEntity roomEntity = lookupAndValidateRoom(organizationId, roomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[RoomOperator] Member {} leaving room {}", memberId, roomName);

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity != null) {

            talkRouter.leaveRoom(organizationId, memberId, roomEntity.getId());

            talkRoomMemberRepository.delete(roomMemberEntity);
        }
    }

    public List<TalkMessageDto> getAllTalkMessagesFromRoom(UUID organizationId, UUID invokingMemberId, String talkRoomName) {

        TalkRoomEntity roomEntity = lookupAndValidateRoom(organizationId, talkRoomName);


        List<TalkRoomMessageEntity> talkMessages = talkRoomMessageRepository.findByToRoomIdOrderByNanoTime(roomEntity.getId());

        List<TalkMessageDto> talkMessageDtos = new ArrayList<>();

        String requestUri = getRequestUriFromContext();

        for (TalkRoomMessageEntity message : talkMessages) {
            TalkMessageDto dto = new TalkMessageDto();
            dto.setId(message.getId());
            dto.setUrn(ROOM_URN_PREFIX + talkRoomName);
            dto.setUri(message.getToRoomId().toString());
            dto.setRequest(requestUri);
            dto.setMessageTime(message.getPosition());
            dto.setNanoTime(message.getNanoTime());
            dto.setMessageType(message.getMessageType().getSimpleClassName());
            dto.setData(JSONTransformer.fromJson(message.getJsonBody(), message.getMessageType().getMessageClazz()));

            //TODO this needs to be joined in a view
            dto.addMetaProp(TalkMessageMetaProp.FROM_MEMBER_ID, message.getFromId().toString());

            MemberDetailsEntity memberDetails = memberDetailsService.lookupMemberDetails(message.getFromId());

            if (memberDetails != null) {
                dto.addMetaProp(TalkMessageMetaProp.FROM_USERNAME, memberDetails.getUsername());
                dto.addMetaProp(TalkMessageMetaProp.FROM_FULLNAME, memberDetails.getFullName());
            }

            talkMessageDtos.add(dto);
        }

        return talkMessageDtos;
    }


    public TalkMessageDto publishSnippetToTalkRoom(UUID organizationId, UUID memberId, String talkRoomId, NewSnippetEventDto newSnippetEventDto) {
        return null;
    }

    public TalkMessageDto publishScreenshotToTalkRoom(UUID organizationId, UUID memberId, String talkRoomId, ScreenshotReferenceInputDto screenshotReferenceInput) {
        return null;
    }

    public TalkMessageDto publishScreenshotToActiveRoom(UUID organizationId, UUID memberId, ScreenshotReferenceInputDto screenshotReferenceInput) {
        return null;
    }

    public TalkMessageDto publishChatToActiveRoom(UUID organizationId, UUID memberId, String chatMessage) {
        return null;
    }

    public TalkMessageDto publishSnippetToActiveRoom(UUID organizationId, UUID memberId, NewSnippetEventDto snippetEventDto) {
        return null;
    }

    private void validateRoomIsFound(TalkRoomEntity roomEntity, String roomName) {
        if (roomEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ROOM, "Unable to find: " + roomName);
        }
    }

}

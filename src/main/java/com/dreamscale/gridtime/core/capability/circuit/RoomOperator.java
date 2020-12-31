package com.dreamscale.gridtime.core.capability.circuit;

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
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
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
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    private TalkRoomMessageRepository talkRoomMessageRepository;

    @Autowired
    private RoomMemberStatusRepository roomMemberStatusRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<CircuitMemberStatusDto, RoomMemberStatusEntity> roomMemberStatusDtoMapper;

    @PostConstruct
    private void init() {
        roomMemberStatusDtoMapper = mapperFactory.createDtoEntityMapper(CircuitMemberStatusDto.class, RoomMemberStatusEntity.class);
    }

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
        return (talkRoomName.length() == 36 && !talkRoomName.contains("_"));
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

        MemberDetailsEntity memberDetails = memberDetailsRetriever.lookupMemberDetails(messageEntity.getFromId());

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

        entityManager.flush();

        talkRouter.joinRoom(organizationId, memberId, roomEntity.getId());

        createAndSendRoomMemberStatusUpdateEvent(now, nanoTime, roomEntity, roomMemberEntity.getMemberId(), CircuitMessageType.ROOM_MEMBER_JOIN);
    }


    private void createAndSendRoomMemberStatusUpdateEvent(LocalDateTime now, Long nanoTime, TalkRoomEntity roomEntity, UUID memberId, CircuitMessageType messageType) {

        String urn = ROOM_URN_PREFIX + roomEntity.getRoomName();

        RoomMemberStatusEntity roomMemberStatus = roomMemberStatusRepository.findByRoomIdAndMemberId(roomEntity.getId(), memberId);

        CircuitMemberStatusDto circuitMemberStatus = roomMemberStatusDtoMapper.toApi(roomMemberStatus);

        RoomMemberStatusEventDto statusDto = new RoomMemberStatusEventDto(messageType.name(), messageType.getStatusMessage(), circuitMemberStatus);

        //dont persist these room member level events in the chat feed, since they are super noisy, consider them transient

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(memberId);
        messageEntity.setToRoomId(roomEntity.getId());
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(statusDto));
        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(roomEntity.getId(), talkMessageDto);
    }


    @Transactional
    public void leaveAllRooms(LocalDateTime now, Long nanoTime, MemberConnectionEntity connection) {

        List<TalkRoomEntity> talkRoomMemberships = talkRoomRepository.findAllRoomsByMembership(connection.getOrganizationId(), connection.getMemberId());

        for (TalkRoomEntity talkRoom : talkRoomMemberships) {

            createAndSendRoomMemberStatusUpdateEvent(now, nanoTime, talkRoom, connection.getMemberId(), CircuitMessageType.ROOM_MEMBER_LEAVE);
        }

        talkRouter.leaveAllRooms(connection, talkRoomMemberships);

        talkRoomMemberRepository.deleteFromAllRooms(connection.getMemberId());

    }

    @Transactional
    public void updateStatusInAllRooms(LocalDateTime now, Long nanoTime, MemberConnectionEntity connection) {
        List<TalkRoomEntity> talkRoomMemberships = talkRoomRepository.findNonTeamRoomsByMembership(connection.getOrganizationId(), connection.getMemberId());

        for (TalkRoomEntity talkRoom : talkRoomMemberships) {

            createAndSendRoomMemberStatusUpdateEvent(now, nanoTime, talkRoom, connection.getMemberId(), CircuitMessageType.ROOM_MEMBER_STATUS_UPDATE);
        }
    }


    @Transactional
    public void leaveRoom(UUID organizationId, UUID memberId, String roomName) {

        TalkRoomEntity roomEntity = lookupAndValidateRoom(organizationId, roomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[RoomOperator] Member {} leaving room {}", memberId, roomName);

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity != null) {

            createAndSendRoomMemberStatusUpdateEvent(now, nanoTime, roomEntity, roomMemberEntity.getMemberId(), CircuitMessageType.ROOM_MEMBER_LEAVE);

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

            MemberDetailsEntity memberDetails = memberDetailsRetriever.lookupMemberDetails(message.getFromId());

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

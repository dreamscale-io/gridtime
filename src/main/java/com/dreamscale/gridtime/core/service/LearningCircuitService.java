package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.event.NewSnippetEvent;
import com.dreamscale.gridtime.core.domain.member.MemberNameEntity;
import com.dreamscale.gridtime.core.domain.member.MemberNameRepository;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitStatusMessageDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.TalkMessageType;
import com.dreamscale.gridtime.core.domain.circuit.RoomMemberStatus;
import com.dreamscale.gridtime.core.domain.circuit.FeedType;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.ChatMessageDto;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LearningCircuitService {

    SillyNameGenerator sillyNameGenerator;

    @Autowired
    private LearningCircuitRepository learningCircuitRepository;

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    private TalkRoomMessageRepository talkRoomMessageRepository;

    @Autowired
    ActiveStatusService activeStatusService;

    @Autowired
    private TimeService timeService;

    @Autowired
    private TalkRouterService talkRouterService;

    @Autowired
    private MemberNameRepository memberNameRepository;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<LearningCircuitDto, LearningCircuitEntity> circuitDtoMapper;

    private static final String DEFAULT_WTF_MESSAGE = "Started WTF";
    private static final String RESUMED_WTF_MESSAGE = "Resumed WTF";


    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circuitDtoMapper = mapperFactory.createDtoEntityMapper(LearningCircuitDto.class, LearningCircuitEntity.class);

        sillyNameGenerator = new SillyNameGenerator();
    }

    @Transactional
    public LearningCircuitDto createNewLearningCircuit(UUID organizationId, UUID memberId) {
        String circuitName = sillyNameGenerator.random();

        return createNewLearningCircuitWithCustomName(organizationId, memberId, circuitName);

    }


    private LearningCircuitEntity tryToSaveAndReserveName(LearningCircuitEntity learningCircuitEntity) {

        LearningCircuitEntity savedEntity = null;
        int retryCounter = 3;

        String requestedCircuitName = learningCircuitEntity.getCircuitName();

        while (savedEntity == null & retryCounter > 0)
        try {
            savedEntity = learningCircuitRepository.save(learningCircuitEntity);
        } catch (Exception ex) {
            learningCircuitEntity.setCircuitName(sillyNameGenerator.randomNewExtension(requestedCircuitName));
            retryCounter--;
        }

        if (savedEntity == null) {
           throw new ConflictException(ConflictErrorCodes.CONFLICTING_CIRCUIT_NAME, "Unable to save Circuit with requested name after 3 tries: "+requestedCircuitName);
        }

        return savedEntity;

    }


    public LearningCircuitDto createNewLearningCircuitWithCustomName(UUID organizationId, UUID memberId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = new LearningCircuitEntity();
        learningCircuitEntity.setId(UUID.randomUUID());
        learningCircuitEntity.setCircuitName(circuitName);
        learningCircuitEntity.setOrganizationId(organizationId);
        learningCircuitEntity.setOwnerId(memberId);
        learningCircuitEntity.setModeratorId(memberId);

        learningCircuitEntity = tryToSaveAndReserveName(learningCircuitEntity);

        //so now I've got a reserved room Id, for my circuit, my wtf room name will automatically be circuit_name/wtf

        LocalDateTime now = timeService.now();

        TalkRoomEntity wtfRoomEntity = new TalkRoomEntity();
        wtfRoomEntity.setId(UUID.randomUUID());
        wtfRoomEntity.setOrganizationOwnerId(organizationId);
        wtfRoomEntity.setCreatedByOwnerId(memberId);
        wtfRoomEntity.setFeedType(FeedType.WTF_EVENT_FEED);
        wtfRoomEntity.setTalkRoomId(deriveWTFTalkRoomId(learningCircuitEntity));

        talkRoomRepository.save(wtfRoomEntity);

        //update circuit with the new room

        learningCircuitEntity.setWtfRoomId(wtfRoomEntity.getId());
        learningCircuitEntity.setOpenTime(now);
        learningCircuitEntity.setCircuitStatus(CircuitStatus.ACTIVE);
        learningCircuitEntity.setCumulativeSecondsBeforeOnHold(0L);
        learningCircuitRepository.save(learningCircuitEntity);

        //then I need to join this new person in the room...

        TalkRoomMemberEntity talkRoomMemberEntity = new TalkRoomMemberEntity();
        talkRoomMemberEntity.setId(UUID.randomUUID());
        talkRoomMemberEntity.setJoinTime(learningCircuitEntity.getOpenTime());
        talkRoomMemberEntity.setLastActive(learningCircuitEntity.getOpenTime());
        talkRoomMemberEntity.setRoomId(wtfRoomEntity.getId());
        talkRoomMemberEntity.setOrganizationId(organizationId);
        talkRoomMemberEntity.setMemberId(memberId);
        talkRoomMemberEntity.setActiveStatus(RoomMemberStatus.ACTIVE);

        talkRoomMemberRepository.save(talkRoomMemberEntity);

        //then update active status

        activeStatusService.pushWTFStatus(organizationId, memberId, learningCircuitEntity.getId(), DEFAULT_WTF_MESSAGE);

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, TalkMessageType.CIRCUIT_OPEN);

        return toDto(learningCircuitEntity);

    }

    private void sendStatusMessageToWTFRoom(LearningCircuitEntity circuit, LocalDateTime now, TalkMessageType messageType) {
        UUID messageId = UUID.randomUUID();
        sendStatusMessage(circuit.getId(), circuit.getCircuitName(), messageId, now,
                circuit.getOwnerId(), circuit.getWtfRoomId(), deriveWTFTalkRoomId(circuit), messageType);
    }

    private void sendStatusMessageToRetroRoom(LearningCircuitEntity circuit, LocalDateTime now, TalkMessageType messageType) {
        UUID messageId = UUID.randomUUID();
        sendStatusMessage(circuit.getId(), circuit.getCircuitName(), messageId, now,
                circuit.getOwnerId(), circuit.getRetroRoomId(), deriveRetroTalkRoomId(circuit), messageType);
    }

    public LearningCircuitDto getCircuit(UUID organizationId, UUID circuitId) {

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findByOrganizationIdAndId(organizationId, circuitId);

        return toDto(circuitEntity);
    }


    public LearningCircuitDto getMyActiveWTFCircuit(UUID organizationId, UUID memberId) {

        //shouldn't generally be more than one, but technically, it's possible during the moments of transition,
        //that multiple circuits could be open at the same time.
        List<LearningCircuitEntity> activeCircuits = learningCircuitRepository.findAllActiveCircuitsOwnedBy(organizationId, memberId);

        LearningCircuitDto circuitDto = null;
        if (activeCircuits != null && activeCircuits.size() > 0) {
             LearningCircuitEntity activeCircuit = activeCircuits.get(0);

             circuitDto = toDto(activeCircuit);

        }

        return circuitDto;
    }

    private LearningCircuitDto toDto(LearningCircuitEntity circuitEntity) {
        LearningCircuitDto circuitDto = circuitDtoMapper.toApi(circuitEntity);

        circuitDto.setWtfTalkRoomId(deriveWTFTalkRoomId(circuitEntity));
        circuitDto.setRetroTalkRoomId(deriveRetroTalkRoomId(circuitEntity));

        return circuitDto;
    }

    private String deriveRetroTalkRoomId(LearningCircuitEntity activeCircuit) {
        if (activeCircuit.getRetroRoomId() != null) {
            return activeCircuit.getCircuitName() + "_retro";
        }
        return null;
    }

    private String deriveWTFTalkRoomId(LearningCircuitEntity activeCircuit) {
        if (activeCircuit.getWtfRoomId() != null) {
            return activeCircuit.getCircuitName() + "_wtf";
        }
        return null;
    }

    @Transactional
    public LearningCircuitDto startRetroForCircuit(UUID organizationId, UUID memberId, String circuitName) {

        //okay so the retro, I'm going to join everyone in the WTF members automatically into the Retro.

        //first get the circuit, then open a room

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        validateRetroNotAlreadyStarted(circuitName, learningCircuitEntity);

        TalkRoomEntity retroRoomEntity = new TalkRoomEntity();
        retroRoomEntity.setId(UUID.randomUUID());
        retroRoomEntity.setOrganizationOwnerId(organizationId);
        retroRoomEntity.setCreatedByOwnerId(memberId);
        retroRoomEntity.setFeedType(FeedType.RETRO_EVENT_FEED);
        retroRoomEntity.setTalkRoomId(deriveRetroTalkRoomId(learningCircuitEntity));

        talkRoomRepository.save(retroRoomEntity);

        //update circuit with the new room

        learningCircuitEntity.setRetroRoomId(retroRoomEntity.getId());
        learningCircuitRepository.save(learningCircuitEntity);

        //then I need to join this new person in the room...

        //now I need to get the members of the WTF room, so I can add them to the retro room.

        List<TalkRoomMemberEntity> wtfRoomMembers = talkRoomMemberRepository.findByRoomId(learningCircuitEntity.getWtfRoomId());
        List<TalkRoomMemberEntity> retroRoomMembers = new ArrayList<>();

        LocalDateTime now = timeService.now();

        for (TalkRoomMemberEntity wtfRoomMember : wtfRoomMembers) {
            TalkRoomMemberEntity retroRoomMember = new TalkRoomMemberEntity();
            retroRoomMember.setId(UUID.randomUUID());
            retroRoomMember.setJoinTime(now);
            retroRoomMember.setLastActive(now);
            retroRoomMember.setRoomId(retroRoomEntity.getId());
            retroRoomMember.setOrganizationId(organizationId);
            retroRoomMember.setMemberId(wtfRoomMember.getMemberId());
            retroRoomMember.setActiveStatus(wtfRoomMember.getActiveStatus());

            retroRoomMembers.add(retroRoomMember);
        }

        talkRoomMemberRepository.save(retroRoomMembers);

        sendStatusMessageToRetroRoom(learningCircuitEntity, now, TalkMessageType.CIRCUIT_RETRO_STARTED);

        return toDto(learningCircuitEntity);

    }

    private void validateRetroNotAlreadyStarted(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getRetroRoomId() != null) {
            throw new ConflictException(ConflictErrorCodes.RETRO_ALREADY_STARTED, "Retro already started for circuit: "+circuitName);
        }
    }

    private void validateCircuitIsActive(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitStatus() == CircuitStatus.ACTIVE) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active: "+circuitName);
        }
    }

    private void validateCircuitIsOnHold(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitStatus() == CircuitStatus.ONHOLD) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be OnHold: "+circuitName);
        }
    }

    private void validateCircuitExists(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find circuit: "+circuitName);
        }
    }

    public LearningCircuitDto joinExistingCircuit(UUID organizationId, UUID memberId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        UUID wtfRoomId = learningCircuitEntity.getWtfRoomId();
        UUID retroRoomId = learningCircuitEntity.getRetroRoomId();

        if (wtfRoomId != null) {
            addMemberToRoom(organizationId, memberId, now, wtfRoomId);
            sendStatusMessageToWTFRoom(learningCircuitEntity, now, TalkMessageType.ROOM_MEMBER_JOIN);

        }

        if (retroRoomId != null) {
            addMemberToRoom(organizationId, memberId, now, retroRoomId);
            sendStatusMessageToRetroRoom(learningCircuitEntity, now, TalkMessageType.ROOM_MEMBER_JOIN);
        }



        return toDto(learningCircuitEntity);
    }

    private void addMemberToRoom(UUID organizationId, UUID memberId, LocalDateTime joinTime, UUID retroRoomId) {
        TalkRoomMemberEntity roomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, retroRoomId, memberId);

        if (roomMember != null) {
            roomMember.setLastActive(joinTime);
            roomMember.setActiveStatus(RoomMemberStatus.ACTIVE);
        } else {
            roomMember = new TalkRoomMemberEntity();
            roomMember.setId(UUID.randomUUID());
            roomMember.setJoinTime(joinTime);
            roomMember.setLastActive(joinTime);
            roomMember.setRoomId(retroRoomId);
            roomMember.setOrganizationId(organizationId);
            roomMember.setMemberId(memberId);
            roomMember.setActiveStatus(RoomMemberStatus.ACTIVE);
        }

        talkRoomMemberRepository.save(roomMember);
    }


    public LearningCircuitDto leaveExistingCircuit(UUID organizationId, UUID memberId, String circuitName) {
        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        UUID wtfRoomId = learningCircuitEntity.getWtfRoomId();
        UUID retroRoomId = learningCircuitEntity.getRetroRoomId();

        if (wtfRoomId != null) {
            markMemberAsInactiveInRoom(organizationId, memberId, now, wtfRoomId);
            sendStatusMessageToWTFRoom(learningCircuitEntity, now, TalkMessageType.ROOM_MEMBER_INACTIVE);
        }

        if (retroRoomId != null) {
            markMemberAsInactiveInRoom(organizationId, memberId, now, retroRoomId);
            sendStatusMessageToRetroRoom(learningCircuitEntity, now, TalkMessageType.ROOM_MEMBER_INACTIVE);
        }

        return toDto(learningCircuitEntity);
    }

    private void markMemberAsInactiveInRoom(UUID organizationId, UUID memberId, LocalDateTime leaveTime, UUID roomId) {

        TalkRoomMemberEntity roomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomId, memberId);

        if (roomMember != null) {
            roomMember.setLastActive(leaveTime);
            roomMember.setActiveStatus(RoomMemberStatus.INACTIVE);

            talkRoomMemberRepository.save(roomMember);
        }
    }

    public LearningCircuitDto closeExistingCircuit(UUID organizationId, UUID memberId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();

        learningCircuitEntity.setCloseTime(now);
        learningCircuitEntity.setCircuitStatus(CircuitStatus.LOCKED);

        learningCircuitRepository.save(learningCircuitEntity);

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, TalkMessageType.CIRCUIT_CLOSED);

        return toDto(learningCircuitEntity);
    }

    @Transactional
    public LearningCircuitDto putCircuitOnHold(UUID organizationId, UUID ownerId, String circuitName) {
        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();

        long durationInSeconds = calculateSecondsBeforeOnHold(learningCircuitEntity, now);
        learningCircuitEntity.setCumulativeSecondsBeforeOnHold(durationInSeconds);

        learningCircuitEntity.setLastOnHoldTime(now);
        learningCircuitEntity.setLastResumeTime(null);
        learningCircuitEntity.setCircuitStatus(CircuitStatus.ONHOLD);

        learningCircuitRepository.save(learningCircuitEntity);

        activeStatusService.resolveWTFWithAbort(organizationId, ownerId);

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, TalkMessageType.CIRCUIT_ONHOLD);

        return toDto(learningCircuitEntity);
    }

    public LearningCircuitDto resumeCircuit(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsOnHold(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();

        learningCircuitEntity.setLastResumeTime(now);
        learningCircuitEntity.setLastOnHoldTime(null);

        learningCircuitEntity.setCircuitStatus(CircuitStatus.ACTIVE);

        learningCircuitRepository.save(learningCircuitEntity);

        activeStatusService.pushWTFStatus(organizationId, ownerId, learningCircuitEntity.getId(), RESUMED_WTF_MESSAGE);

        LearningCircuitDto circuitDto =  toDto(learningCircuitEntity);

        circuitDto.setCumulativeSecondsBeforeLastOnHold(calculateEffectiveDuration(circuitDto));

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, TalkMessageType.CIRCUIT_RESUMED);

        return circuitDto;
    }

    private long calculateSecondsBeforeOnHold(LearningCircuitEntity circuitEntity, LocalDateTime now) {
        long totalDuration = 0;

        if (circuitEntity.getCumulativeSecondsBeforeOnHold() != null) {
            totalDuration = circuitEntity.getCumulativeSecondsBeforeOnHold();
        }

        //either take the additional time from start, or from resume
        if (circuitEntity.getLastResumeTime() == null) {
            long additionalDuration = ChronoUnit.SECONDS.between(circuitEntity.getOpenTime(), now);
            totalDuration += additionalDuration;
        } else {
            long additionalDuration = ChronoUnit.SECONDS.between(circuitEntity.getLastResumeTime(), now);
            totalDuration += additionalDuration;
        }

        return totalDuration;
    }

    private Long calculateEffectiveDuration(LearningCircuitDto circuitDto) {
        LocalDateTime startTimer = circuitDto.getOpenTime();

        if (circuitDto.getLastResumeTime() != null) {
            startTimer = circuitDto.getLastResumeTime();
        }

        long seconds = startTimer.until(timeService.now(), ChronoUnit.SECONDS);
        seconds += circuitDto.getCumulativeSecondsBeforeLastOnHold();

        return seconds;
    }


    public CircuitMessageDto publishChatToWTFRoom(UUID organizationId, UUID fromMemberId, String circuitName, String chatMessage) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        UUID messageId = UUID.randomUUID();

        String talkWtfRoomId = deriveWTFTalkRoomId(learningCircuitEntity);

        return sendRoomMessage(messageId, now, fromMemberId, learningCircuitEntity.getWtfRoomId(), talkWtfRoomId, chatMessage);
    }

    private CircuitMessageDto toDto(TalkRoomMessageEntity messageEntity, String message) {

        CircuitMessageDto messageDto = new CircuitMessageDto();
        messageDto.setMessageId(messageEntity.getId());
        messageDto.setMessage(message);
        messageDto.setMessageFrom(lookupMemberDto(messageEntity.getFromId()));
        messageDto.setMessageTime(messageEntity.getPosition());
        messageDto.setMessageType(messageEntity.getMessageType().name());

        return messageDto;
    }

    private CircuitMemberDto lookupMemberDto(UUID memberId) {
        MemberNameEntity memberNameInfo = memberNameRepository.findByMemberId(memberId);

        CircuitMemberDto memberDto = new CircuitMemberDto();
        memberDto.setMemberId(memberId);
        if (memberNameInfo != null) {
            memberDto.setFullName(memberNameInfo.getFullName());
            memberDto.setShortName(memberNameInfo.getShortName());
        }

        return memberDto;
    }

    private void updateMemberStatusWithTouch(UUID roomId, UUID memberId) {
        //TODO can do this stuff later
        //activeStatusService.touchActivity(memberId);
    }

    public CircuitMessageDto publishChatToRetroFeed(UUID organizationId, UUID fromMemberId, String circuitName, String chatMessage) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        UUID messageId = UUID.randomUUID();

        if (learningCircuitEntity.getRetroRoomId() != null) {

            UUID roomId = learningCircuitEntity.getRetroRoomId();
            String retroTalkRoomId = deriveRetroTalkRoomId(learningCircuitEntity);

            return sendRoomMessage(messageId, now, fromMemberId, roomId, retroTalkRoomId, chatMessage);

        }
        return null;
    }

    private CircuitMessageDto sendStatusMessage(UUID circuitId, String circuitName, UUID messageId, LocalDateTime now, UUID fromMemberId, UUID roomId,
                                                      String talkRoomId, TalkMessageType messageType) {

        talkRouterService.sendAsyncRoomMessage(messageId, now, fromMemberId, talkRoomId, messageType.getStatusMessage());

        updateMemberStatusWithTouch(roomId, fromMemberId);

        CircuitStatusMessageDto msg = new CircuitStatusMessageDto(circuitId, circuitName, messageType.name(), messageType.getStatusMessage());

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(msg));

        talkRoomMessageRepository.save(messageEntity);

        return toDto(messageEntity, messageType.getStatusMessage());
    }


    private CircuitMessageDto sendRoomMessage(UUID messageId, LocalDateTime now, UUID fromMemberId, UUID roomId, String talkRoomId, String chatMessage) {
        talkRouterService.sendAsyncRoomMessage(messageId, now, fromMemberId, talkRoomId, chatMessage);

        updateMemberStatusWithTouch(roomId, fromMemberId);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setMessageType(TalkMessageType.CHAT);
        messageEntity.setJsonBody(JSONTransformer.toJson(new ChatMessageDto(chatMessage)));

        talkRoomMessageRepository.save(messageEntity);

        return toDto(messageEntity, chatMessage);
    }

    public List<LearningCircuitDto> getMyDoItLaterCircuits(UUID organizationId, UUID memberId) {
        return null;
    }

    public CircuitMessageDto postScreenshotReferenceToCircuitFeed(UUID organizationId, UUID spiritId, UUID circleId, ScreenshotReferenceInputDto screenshotReferenceInputDto) {

        //TODO map to the new circuit stuff
        //        CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
//        circleMessageEntity.setId(UUID.randomUUID());
//        circleMessageEntity.setTorchieId(spiritId);
//        circleMessageEntity.setPosition(timeService.now());
//
//        circleMessageEntity.setCircleId(circleId);
//        circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Added screenshot for " + screenshotReferenceInputDto.getFileName());
//        circleMessageEntity.setMetadataField(CircleMessageMetadataField.name, screenshotReferenceInputDto.getFileName());
//        circleMessageEntity.setMetadataField(CircleMessageMetadataField.filePath, screenshotReferenceInputDto.getFilePath());
//        circleMessageEntity.setMessageType(CircuitMessageType.SCREENSHOT);
//
//        circleMessageRepository.save(circleMessageEntity);
//
//        CircuitMessageDto circuitMessageDto = feedMessageMapper.toApi(circleMessageEntity);
//
//        circuitMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.message));
//        circuitMessageDto.setFileName(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.name));
//        circuitMessageDto.setFilePath(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.filePath));
//
//        circuitMessageDto.setMessageFrom(createCircleMember(spiritId));
//        return circuitMessageDto;
        return null;
    }

    public CircuitMessageDto postSnippetToActiveCircuitFeed(UUID organizationId, UUID torchieId, NewSnippetEvent snippetEvent) {

        //TODO map to the new circuit stuff

//        UUID activeCircleId = activeStatusService.getActiveCircleId(organizationId, torchieId);
//
//        CircuitMessageDto circuitMessageDto = null;
//
//        if (activeCircleId != null) {
//
//            CircleMessageEntity circleMessageEntity = new CircleMessageEntity();
//            circleMessageEntity.setId(UUID.randomUUID());
//            circleMessageEntity.setTorchieId(torchieId);
//            circleMessageEntity.setPosition(timeService.now());
//
//            circleMessageEntity.setCircleId(activeCircleId);
//            circleMessageEntity.setMetadataField(CircleMessageMetadataField.message, "Added snippet from " + snippetEvent.getSource());
//            circleMessageEntity.setMetadataField(CircleMessageMetadataField.snippetSource, snippetEvent.getSource());
//            circleMessageEntity.setMetadataField(CircleMessageMetadataField.snippet, snippetEvent.getSnippet());
//            circleMessageEntity.setMessageType(CircuitMessageType.SNIPPET);
//
//            circleMessageRepository.save(circleMessageEntity);
//
//            circuitMessageDto = feedMessageMapper.toApi(circleMessageEntity);
//
//            circuitMessageDto.setMessage(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.message));
//            circuitMessageDto.setSnippetSource(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.snippetSource));
//            circuitMessageDto.setSnippet(circleMessageEntity.getMetadataValue(CircleMessageMetadataField.snippet));
//
//            circuitMessageDto.setMessageFrom(createCircleMember(torchieId));
//        }
//
//        return circuitMessageDto;
        return null;
    }

    public List<CircuitMessageDto> getAllMessagesForCircuitFeed(UUID organizationId, UUID torchieId, UUID circleId) {

//        List<CircleFeedMessageEntity> messageEntities = circleFeedWithMembersRepository.findByCircleIdOrderByPosition(circleId);
//
//        List<CircuitMessageDto> circuitMessageDtos = new ArrayList<>();
//
//        for (CircleFeedMessageEntity messageEntity : messageEntities) {
//            CircuitMessageDto circuitMessageDto = circleFeedMessageMapper.toApi(messageEntity);
//            circuitMessageDto.setMessage(messageEntity.getMetadataValue(CircleMessageMetadataField.message));
//            circuitMessageDto.setMessageFrom(createCircleMember(torchieId, messageEntity.getFullName()));
//
//            circuitMessageDtos.add(circuitMessageDto);
//        }
//
//        return circuitMessageDtos;

        return null;
    }

    public List<LearningCircuitDto> getAllParticipatingCircles(UUID organizationId, UUID torchieId) {
        return null;
    }


}

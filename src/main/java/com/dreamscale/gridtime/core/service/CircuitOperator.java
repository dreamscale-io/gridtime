package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsRepository;
import com.dreamscale.gridtime.api.circuit.CircuitStatusDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.domain.circuit.RoomMemberStatus;
import com.dreamscale.gridtime.core.domain.circuit.RoomType;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
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
public class CircuitOperator {

    public static final String RETRO_ROOM_SUFFIX = "-retro";
    public static final String WTF_ROOM_SUFFIX = "-wtf";
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
    private LearningCircuitRoomRepository learningCircuitRoomRepository;

    @Autowired
    ActiveStatusService activeStatusService;

    @Autowired
    private TimeService timeService;

    @Autowired
    private GridTalkRouter talkRouter;
    @Autowired
    private CircuitMemberStatusRepository circuitMemberStatusRepository;

    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private MapperFactory mapperFactory;


    private DtoEntityMapper<LearningCircuitDto, LearningCircuitEntity> circuitDtoMapper;
    private DtoEntityMapper<LearningCircuitWithMembersDto, LearningCircuitEntity> circuitFullDtoMapper;
    private DtoEntityMapper<CircuitMemberStatusDto, CircuitMemberStatusEntity> circuitMemberDtoMapper;

    private static final String DEFAULT_WTF_MESSAGE = "Started WTF";
    private static final String RESUMED_WTF_MESSAGE = "Resumed WTF";


    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circuitDtoMapper = mapperFactory.createDtoEntityMapper(LearningCircuitDto.class, LearningCircuitEntity.class);
        circuitFullDtoMapper = mapperFactory.createDtoEntityMapper(LearningCircuitWithMembersDto.class, LearningCircuitEntity.class);
        circuitMemberDtoMapper = mapperFactory.createDtoEntityMapper(CircuitMemberStatusDto.class, CircuitMemberStatusEntity.class);

        sillyNameGenerator = new SillyNameGenerator();
    }

    @Transactional
    public LearningCircuitDto createNewLearningCircuit(UUID organizationId, UUID memberId) {
        String circuitName = sillyNameGenerator.random();

        log.info("Creating new circuit : " + circuitName);
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
            throw new ConflictException(ConflictErrorCodes.CONFLICTING_CIRCUIT_NAME, "Unable to save Circuit with requested name after 3 tries: " + requestedCircuitName);
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
        Long nanoTime = timeService.nanoTime();

        TalkRoomEntity wtfRoomEntity = new TalkRoomEntity();
        wtfRoomEntity.setId(UUID.randomUUID());
        wtfRoomEntity.setOrganizationId(organizationId);
        wtfRoomEntity.setRoomType(RoomType.WTF_ROOM);
        wtfRoomEntity.setRoomName(deriveWTFRoomName(learningCircuitEntity));

        talkRoomRepository.save(wtfRoomEntity);

        //update circuit with the new room

        learningCircuitEntity.setWtfRoomId(wtfRoomEntity.getId());
        learningCircuitEntity.setOpenTime(now);
        learningCircuitEntity.setCircuitStatus(CircuitStatus.ACTIVE);
        learningCircuitEntity.setSecondsBeforeOnHold(0L);
        learningCircuitRepository.save(learningCircuitEntity);

        //then I need to join this new person in the room...

        TalkRoomMemberEntity talkRoomMemberEntity = new TalkRoomMemberEntity();
        talkRoomMemberEntity.setId(UUID.randomUUID());
        talkRoomMemberEntity.setJoinTime(learningCircuitEntity.getOpenTime());
        talkRoomMemberEntity.setLastActive(learningCircuitEntity.getOpenTime());
        talkRoomMemberEntity.setRoomId(wtfRoomEntity.getId());
        talkRoomMemberEntity.setOrganizationId(organizationId);
        talkRoomMemberEntity.setMemberId(memberId);
        talkRoomMemberEntity.setRoomStatus(RoomMemberStatus.ACTIVE);

        talkRoomMemberRepository.save(talkRoomMemberEntity);

        talkRouter.joinRoom(organizationId, memberId, wtfRoomEntity.getId());
        //then update active status

        activeStatusService.pushWTFStatus(organizationId, memberId, learningCircuitEntity.getId());

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.CIRCUIT_OPEN);

        return toDto(learningCircuitEntity);

    }

    private void sendStatusMessageToWTFRoom(LearningCircuitEntity circuit, LocalDateTime now, Long nanoTime, CircuitMessageType messageType) {

        sendCircuitStatusMessage(circuit.getId(), circuit.getCircuitName(), circuit.getOwnerId(), now, nanoTime,
                circuit.getWtfRoomId(), messageType);
    }

    private void sendStatusMessageToRetroRoom(LearningCircuitEntity circuit, LocalDateTime now, Long nanoTime, CircuitMessageType messageType) {
        sendCircuitStatusMessage(circuit.getId(), circuit.getCircuitName(), circuit.getOwnerId(), now, nanoTime,
               circuit.getRetroRoomId(), messageType);
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

    public LearningCircuitWithMembersDto getCircuitWithAllDetails(UUID organizationId, String circuitName) {

        log.info("inside getCircuitWithAllDetails");


        LearningCircuitEntity circuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, circuitEntity);

        //this returns nulls in the list for some reason?  Maybe the full outer join...?
        List<CircuitMemberStatusEntity> circuitMembers = circuitMemberStatusRepository.findByCircuitId(circuitEntity.getId());

        LearningCircuitWithMembersDto fullDto = toFullDetailsDto(circuitEntity);

        for (CircuitMemberStatusEntity memberStatus : circuitMembers) {
            CircuitMemberStatusDto memberStatusDto = circuitMemberDtoMapper.toApi(memberStatus);

            if (memberStatusDto != null) {
                fullDto.addCircuitMember(memberStatusDto);
            }
        }


        return fullDto;
    }

    private LearningCircuitDto toDto(LearningCircuitEntity circuitEntity) {
        LearningCircuitDto circuitDto = circuitDtoMapper.toApi(circuitEntity);

        if (circuitEntity != null) {
            if (circuitEntity.getWtfRoomId() != null) {
                circuitDto.setWtfTalkRoomId(circuitEntity.getWtfRoomId());
                circuitDto.setWtfTalkRoomName(deriveWTFRoomName(circuitEntity));
            }

            if (circuitEntity.getRetroRoomId() != null) {
                circuitDto.setRetroTalkRoomId(circuitEntity.getRetroRoomId());
                circuitDto.setRetroTalkRoomName(deriveRetroTalkRoomId(circuitEntity));
            }

            if (circuitEntity.getJsonTags() != null) {
                TagsInputDto tagsInput = JSONTransformer.fromJson(circuitEntity.getJsonTags(), TagsInputDto.class);
                circuitDto.setTags(tagsInput.getTags());
            }
        }

        return circuitDto;
    }

    private LearningCircuitWithMembersDto toFullDetailsDto(LearningCircuitEntity circuitEntity) {
        LearningCircuitWithMembersDto circuitDto = circuitFullDtoMapper.toApi(circuitEntity);

        if (circuitEntity.getWtfRoomId() != null) {
            circuitDto.setWtfTalkRoomId(circuitEntity.getWtfRoomId());
            circuitDto.setWtfTalkRoomName(deriveWTFRoomName(circuitEntity));
        }

        if (circuitEntity.getRetroRoomId() != null) {
            circuitDto.setRetroTalkRoomId(circuitEntity.getRetroRoomId());
            circuitDto.setRetroTalkRoomName(deriveRetroTalkRoomId(circuitEntity));
        }

        return circuitDto;
    }

    private String deriveRetroTalkRoomId(LearningCircuitEntity activeCircuit) {
        return activeCircuit.getCircuitName() + RETRO_ROOM_SUFFIX;
    }

    private String deriveWTFRoomName(LearningCircuitEntity activeCircuit) {
        return activeCircuit.getCircuitName() + WTF_ROOM_SUFFIX;
    }

    @Transactional
    public LearningCircuitDto startRetroForCircuit(UUID organizationId, UUID memberId, String circuitName) {

        //okay so the retro, I'm going to join everyone in the WTF_ROOM members automatically into the Retro.

        //first get the circuit, then open a room

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        validateRetroNotAlreadyStarted(circuitName, learningCircuitEntity);

        TalkRoomEntity retroRoomEntity = new TalkRoomEntity();
        retroRoomEntity.setId(UUID.randomUUID());
        retroRoomEntity.setOrganizationId(organizationId);
        retroRoomEntity.setRoomType(RoomType.RETRO_ROOM);
        retroRoomEntity.setRoomName(deriveRetroTalkRoomId(learningCircuitEntity));

        talkRoomRepository.save(retroRoomEntity);

        //update circuit with the new room

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        learningCircuitEntity.setRetroStartedTime(now);
        learningCircuitEntity.setRetroRoomId(retroRoomEntity.getId());

        learningCircuitRepository.save(learningCircuitEntity);

        //then I need to join this new person in the room...

        //now I need to get the members of the WTF_ROOM room, so I can add them to the retro room.

        List<TalkRoomMemberEntity> wtfRoomMembers = talkRoomMemberRepository.findByRoomId(learningCircuitEntity.getWtfRoomId());
        List<TalkRoomMemberEntity> retroRoomMembers = new ArrayList<>();

        for (TalkRoomMemberEntity wtfRoomMember : wtfRoomMembers) {
            TalkRoomMemberEntity retroRoomMember = new TalkRoomMemberEntity();
            retroRoomMember.setId(UUID.randomUUID());
            retroRoomMember.setJoinTime(now);
            retroRoomMember.setLastActive(now);
            retroRoomMember.setRoomId(retroRoomEntity.getId());
            retroRoomMember.setOrganizationId(organizationId);
            retroRoomMember.setMemberId(wtfRoomMember.getMemberId());
            retroRoomMember.setRoomStatus(wtfRoomMember.getRoomStatus());

            talkRouter.joinRoom(organizationId, wtfRoomMember.getMemberId(), retroRoomEntity.getId());

            retroRoomMembers.add(retroRoomMember);
        }

        talkRoomMemberRepository.save(retroRoomMembers);

        sendStatusMessageToRetroRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.CIRCUIT_RETRO_STARTED);

        return toDto(learningCircuitEntity);

    }

    private void validateRetroNotAlreadyStarted(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getRetroRoomId() != null) {
            throw new ConflictException(ConflictErrorCodes.RETRO_ALREADY_STARTED, "Retro already started for circuit: " + circuitName);
        }
    }

    private void validateCircuitIsActive(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitStatus() != CircuitStatus.ACTIVE) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active: " + circuitName);
        }
    }

    private void validateCircuitIsOnHold(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitStatus() != CircuitStatus.ONHOLD) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be OnHold: " + circuitName);
        }
    }

    private void validateCircuitExists(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find: " + circuitName);
        }
    }

    private void validateMemberInRoom(UUID organizationId, UUID invokingMemberId, String talkRoomName) {
        log.info("org={}, member={}, room={}", organizationId, invokingMemberId, talkRoomName);

        TalkRoomMemberEntity foundRoomMember = talkRoomMemberRepository.findByOrganizationMemberAndTalkRoomId(organizationId, invokingMemberId, talkRoomName);
        if (foundRoomMember == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ACCESS_TO_CIRCUIT, "Unable to access talk room: " + talkRoomName);
        }
    }

    public LearningCircuitDto joinExistingCircuit(UUID organizationId, UUID memberId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();
        UUID wtfRoomId = learningCircuitEntity.getWtfRoomId();
        UUID retroRoomId = learningCircuitEntity.getRetroRoomId();

        if (wtfRoomId != null) {
            addMemberToRoom(organizationId, memberId, now, wtfRoomId);
            sendStatusMessageToWTFRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.ROOM_MEMBER_JOIN);

        }

        if (retroRoomId != null) {
            addMemberToRoom(organizationId, memberId, now, retroRoomId);
            sendStatusMessageToRetroRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.ROOM_MEMBER_JOIN);
        }


        return toDto(learningCircuitEntity);
    }

    private void addMemberToRoom(UUID organizationId, UUID memberId, LocalDateTime joinTime, UUID retroRoomId) {
        TalkRoomEntity roomEntity = talkRoomRepository.findById(retroRoomId);
        TalkRoomMemberEntity roomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, retroRoomId, memberId);

        if (roomMember != null) {
            roomMember.setLastActive(joinTime);
            roomMember.setRoomStatus(RoomMemberStatus.ACTIVE);
        } else {
            roomMember = new TalkRoomMemberEntity();
            roomMember.setId(UUID.randomUUID());
            roomMember.setJoinTime(joinTime);
            roomMember.setLastActive(joinTime);
            roomMember.setRoomId(retroRoomId);
            roomMember.setOrganizationId(organizationId);
            roomMember.setMemberId(memberId);
            roomMember.setRoomStatus(RoomMemberStatus.ACTIVE);
        }

        talkRouter.joinRoom(organizationId, memberId, roomEntity.getId());
        talkRoomMemberRepository.save(roomMember);
    }


    public LearningCircuitDto leaveExistingCircuit(UUID organizationId, UUID memberId, String circuitName) {
        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();
        UUID wtfRoomId = learningCircuitEntity.getWtfRoomId();
        UUID retroRoomId = learningCircuitEntity.getRetroRoomId();

        if (wtfRoomId != null) {
            updateRoomMemberAsInactiveAndLeaveRoomInTalk(organizationId, memberId, now, wtfRoomId);
            sendStatusMessageToWTFRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.ROOM_MEMBER_INACTIVE);
        }

        if (retroRoomId != null) {
            updateRoomMemberAsInactiveAndLeaveRoomInTalk(organizationId, memberId, now, retroRoomId);
            sendStatusMessageToRetroRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.ROOM_MEMBER_INACTIVE);
        }

        return toDto(learningCircuitEntity);
    }

    private void updateRoomMemberAsInactiveAndLeaveRoomInTalk(UUID organizationId, UUID memberId, LocalDateTime leaveTime, UUID roomId) {

        updateRoomMemberToInactive(organizationId, memberId, leaveTime, roomId);

        talkRouter.leaveRoom(organizationId, memberId, roomId);
    }

    private void updateRoomMemberToInactive(UUID organizationId, UUID memberId, LocalDateTime leaveTime, UUID roomId) {

        TalkRoomMemberEntity roomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomId, memberId);

        if (roomMember != null) {
            roomMember.setLastActive(leaveTime);
            roomMember.setRoomStatus(RoomMemberStatus.INACTIVE);

            talkRoomMemberRepository.save(roomMember);
        }
    }

    public LearningCircuitDto closeExistingCircuit(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.CIRCUIT_CLOSED);

        learningCircuitEntity.setCloseTime(now);
        learningCircuitEntity.setCircuitStatus(CircuitStatus.CLOSED);

        learningCircuitRepository.save(learningCircuitEntity);

        activeStatusService.resolveWTFWithYay(organizationId, ownerId);

        //retro room is still open

        talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());

        return toDto(learningCircuitEntity);
    }

    @Transactional
    public LearningCircuitDto putCircuitOnHoldWithDoItLater(UUID organizationId, UUID ownerId, String circuitName) {
        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        long durationInSeconds = calculateSecondsBeforeOnHold(learningCircuitEntity, now);
        learningCircuitEntity.setSecondsBeforeOnHold(durationInSeconds);

        learningCircuitEntity.setLastOnHoldTime(now);
        learningCircuitEntity.setLastResumeTime(null);
        learningCircuitEntity.setCircuitStatus(CircuitStatus.ONHOLD);

        learningCircuitRepository.save(learningCircuitEntity);

        if (learningCircuitEntity.getWtfRoomId() != null) {
            talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());
        }
        if (learningCircuitEntity.getRetroRoomId() != null) {
            talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getRetroRoomId());
        }

        activeStatusService.resolveWTFWithAbort(organizationId, ownerId);

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.CIRCUIT_ONHOLD);

        return toDto(learningCircuitEntity);
    }

    public LearningCircuitDto resumeCircuit(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsOnHold(circuitName, learningCircuitEntity);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        learningCircuitEntity.setLastResumeTime(now);
        learningCircuitEntity.setLastOnHoldTime(null);

        learningCircuitEntity.setCircuitStatus(CircuitStatus.ACTIVE);

        learningCircuitRepository.save(learningCircuitEntity);

        if (learningCircuitEntity.getWtfRoomId() != null) {
            talkRouter.reviveRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());
        }
        if (learningCircuitEntity.getRetroRoomId() != null) {
            talkRouter.reviveRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getRetroRoomId());
        }

        activeStatusService.pushWTFStatus(organizationId, ownerId, learningCircuitEntity.getId());

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        circuitDto.setSecondsBeforeOnHold(calculateEffectiveDuration(circuitDto));

        sendStatusMessageToWTFRoom(learningCircuitEntity, now, nanoTime, CircuitMessageType.CIRCUIT_RESUMED);

        return circuitDto;
    }

    private long calculateSecondsBeforeOnHold(LearningCircuitEntity circuitEntity, LocalDateTime now) {
        long totalDuration = 0;

        if (circuitEntity.getSecondsBeforeOnHold() != null) {
            totalDuration = circuitEntity.getSecondsBeforeOnHold();
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
        seconds += circuitDto.getSecondsBeforeOnHold();

        return seconds;
    }

    public TalkMessageDto publishChatToTalkRoom(UUID organizationId, UUID fromMemberId, String talkRoomName, String chatMessage) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findCircuitByOrganizationAndRoomName(organizationId, talkRoomName);

        validateCircuitExists(talkRoomName, learningCircuitEntity);
        validateCircuitIsActive(talkRoomName, learningCircuitEntity);

        validateMemberInRoom(organizationId, fromMemberId, talkRoomName);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();
        UUID messageId = UUID.randomUUID();

        UUID roomId = getRoomIdBasedOnTalkRoomId(learningCircuitEntity, talkRoomName);

        return sendRoomMessage(messageId, now, nanoTime, fromMemberId, roomId, chatMessage);
    }

    private UUID getRoomIdBasedOnTalkRoomId(LearningCircuitEntity learningCircuitEntity, String talkRoomId) {
        if (talkRoomId.endsWith(RETRO_ROOM_SUFFIX)) {
            return learningCircuitEntity.getRetroRoomId();
        } else {
            return learningCircuitEntity.getWtfRoomId();
        }
    }



    private TalkMessageDto toTalkMessageDto(TalkRoomMessageEntity messageEntity) {

        TalkMessageDto messageDto = new TalkMessageDto();
        messageDto.setId(messageEntity.getId());
        messageDto.setUri(messageEntity.getToRoomId().toString());
        messageDto.setJsonBody(messageEntity.getJsonBody());

        messageDto.addMetaProp(TalkMessageMetaProps.FROM_MEMBER_ID, messageEntity.getFromId().toString());

        messageDto.setMessageTime(messageEntity.getPosition());
        messageDto.setNanoTime(messageEntity.getNanoTime());
        messageDto.setMessageType(messageEntity.getMessageType().getSimpleClassName());

        return messageDto;
    }


    private void updateMemberStatusWithTouch(UUID roomId, UUID memberId) {
        //TODO can do this stuff later
        //activeStatusService.touchActivity(memberId);
    }

    private TalkMessageDto sendCircuitStatusMessage(UUID circuitId, String circuitName, UUID circuitOwnerId, LocalDateTime now, Long nanoTime, UUID roomId, CircuitMessageType messageType) {

        CircuitStatusDto statusDto = new CircuitStatusDto(circuitId, circuitName, messageType.name(), messageType.getStatusMessage());

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(circuitOwnerId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(statusDto));

        TalkMessageDto talkMessageDto = toTalkMessageDto(messageEntity);

        talkRouter.sendAsyncRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        return talkMessageDto;
    }

    private TalkMessageDto sendMemberStatusMessage(UUID circuitOwnerId, UUID memberId, LocalDateTime now, Long nanoTime, UUID roomId, CircuitMessageType messageType) {

        RoomMemberStatusEventDto statusDto = new RoomMemberStatusEventDto(memberId, messageType.name(), messageType.getStatusMessage());

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(circuitOwnerId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(statusDto));

        TalkMessageDto talkMessageDto = toTalkMessageDto(messageEntity);

        talkRouter.sendAsyncRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        return talkMessageDto;
    }


    private TalkMessageDto sendRoomMessage(UUID messageId, LocalDateTime now, Long nanoTime, UUID fromMemberId, UUID roomId, String chatMessage) {

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.CHAT);
        messageEntity.setJsonBody(JSONTransformer.toJson(new ChatMessageDetailsDto(chatMessage)));

        TalkMessageDto talkMessageDto = toTalkMessageDto(messageEntity);

        talkRouter.sendAsyncRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        updateMemberStatusWithTouch(roomId, fromMemberId);

        return talkMessageDto;
    }

    private TalkMessageDto sendSnippetMessage(UUID messageId, LocalDateTime now, Long nanoTime, UUID fromMemberId, UUID roomId, NewSnippetEventDto snippet) {

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.SNIPPET);
        messageEntity.setJsonBody(JSONTransformer.toJson(createSnippetMessage(snippet)));

        TalkMessageDto talkMessageDto = toTalkMessageDto(messageEntity);

        talkRouter.sendAsyncRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        updateMemberStatusWithTouch(roomId, fromMemberId);

        return talkMessageDto;
    }

    private SnippetMessageDetailsDto createSnippetMessage(NewSnippetEventDto snippet) {

        SnippetMessageDetailsDto snippetMessage = new SnippetMessageDetailsDto();
        snippetMessage.setSourceType(snippet.getSource().name());
        snippetMessage.setFilePath(snippet.getFilePath());
        snippetMessage.setLineNumber(snippet.getLineNumber());
        snippetMessage.setSnippet(snippet.getSnippet());

        return snippetMessage;
    }

    public List<LearningCircuitDto> getMyDoItLaterCircuits(UUID organizationId, UUID memberId) {

        List<LearningCircuitEntity> circuits = learningCircuitRepository.findAllOnHoldCircuitsOwnedBy(organizationId, memberId);

        List<LearningCircuitDto> doItLaterCircuits = new ArrayList<>();

        for (LearningCircuitEntity circuit : circuits) {
            doItLaterCircuits.add(toDto(circuit));
        }

        return doItLaterCircuits;
    }


    public List<TalkMessageDto> getAllTalkMessagesFromRoom(UUID organizationId, UUID invokingMemberId, String talkRoomName) {

        validateMemberInRoom(organizationId, invokingMemberId, talkRoomName);

        List<TalkRoomMessageEntity> talkMessages = talkRoomMessageRepository.findByTalkRoomName(talkRoomName);

        List<TalkMessageDto> talkMessageDtos = new ArrayList<>();

        for (TalkRoomMessageEntity message : talkMessages) {
            TalkMessageDto dto = new TalkMessageDto();
            dto.setId(message.getId());
            dto.setUri(message.getToRoomId().toString());
            dto.setMessageTime(message.getPosition());
            dto.setNanoTime(message.getNanoTime());
            dto.setMessageType(message.getMessageType().getSimpleClassName());
            dto.setJsonBody(message.getJsonBody());

            talkMessageDtos.add(dto);
        }

        return talkMessageDtos;
    }


    public void notifyRoomsOfMemberDisconnect(UUID oldConnectionId) {

        MemberConnectionEntity memberConnection = memberConnectionRepository.findByConnectionId(oldConnectionId);

        if (memberConnection != null) {

            LocalDateTime now = timeService.now();
            Long nanoTime = timeService.nanoTime();

            List<LearningCircuitRoomEntity> circuitRooms = learningCircuitRoomRepository.findRoomsByMembership(memberConnection.getOrganizationId(), memberConnection.getMemberId());

            for (LearningCircuitRoomEntity circuitRoom : circuitRooms) {

                updateRoomMemberToInactive(memberConnection.getOrganizationId(), memberConnection.getMemberId(), timeService.now(), circuitRoom.getRoomId());

                sendMemberStatusMessage(circuitRoom.getCircuitOwnerId(), memberConnection.getMemberId(), now, nanoTime, circuitRoom.getRoomId(), CircuitMessageType.ROOM_MEMBER_INACTIVE);
            }
        }
    }

    public LearningCircuitDto saveDescriptionForLearningCircuit(UUID organizationId, UUID ownerId, String circuitName, DescriptionInputDto descriptionInputDto) {

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, circuitEntity);

        circuitEntity.setDescription(descriptionInputDto.getDescription());

        learningCircuitRepository.save(circuitEntity);

        return toDto(circuitEntity);
    }

    public LearningCircuitDto saveTagsForLearningCircuit(UUID organizationId, UUID ownerId, String circuitName, TagsInputDto tagsInputDto) {

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, circuitEntity);

        circuitEntity.setJsonTags(JSONTransformer.toJson(tagsInputDto));

        learningCircuitRepository.save(circuitEntity);

        return toDto(circuitEntity);
    }

    public TalkMessageDto publishSnippetToActiveCircuit(UUID organizationId, UUID memberId, NewSnippetEventDto newSnippetEventDto) {
        LearningCircuitDto learningCircuitDto = getMyActiveWTFCircuit(organizationId, memberId);

        TalkMessageDto messageDto = null;
        if (learningCircuitDto != null) {

            LocalDateTime now = timeService.now();
            Long nanoTime = timeService.nanoTime();
            UUID messageId = UUID.randomUUID();

            messageDto = sendSnippetMessage(messageId, now, nanoTime, memberId, learningCircuitDto.getWtfTalkRoomId(), newSnippetEventDto);
        }

        return messageDto;
    }


    public List<LearningCircuitDto> getAllParticipatingCircuits(UUID organizationId, UUID memberId) {

        List<LearningCircuitEntity> circuits = learningCircuitRepository.findAllParticipatingCircuits(organizationId, memberId);

        List<LearningCircuitDto> participatingCircuits = new ArrayList<>();

        for (LearningCircuitEntity circuit : circuits) {
            participatingCircuits.add(toDto(circuit));
        }

        return participatingCircuits;
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

    public List<LearningCircuitDto> getAllParticipatingCircuitsForOtherMember(UUID organizationId, UUID id, UUID otherMemberId) {
        return null;
    }



}

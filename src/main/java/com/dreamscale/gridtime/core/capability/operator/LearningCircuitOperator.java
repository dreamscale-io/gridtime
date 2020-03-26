package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.api.circuit.CircuitStatusDto;
import com.dreamscale.gridtime.core.capability.directory.DictionaryCapability;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
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
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.service.GridClock;
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
public class LearningCircuitOperator {

    public static final String RETRO_ROOM_SUFFIX = "-retro";
    public static final String WTF_ROOM_SUFFIX = "-wtf";
    public static final String STATUS_ROOM_SUFFIX = "-status";

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
    private LearningCircuitMemberRepository learningCircuitMemberRepository;

    @Autowired
    ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private TeamCircuitOperator teamCircuitOperator;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private DictionaryCapability dictionaryCapability;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private RoomMemberStatusRepository roomMemberStatusRepository;

    @Autowired
    private CircuitMemberStatusRepository circuitMemberStatusRepository;

    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private MapperFactory mapperFactory;


    private DtoEntityMapper<LearningCircuitDto, LearningCircuitEntity> circuitDtoMapper;
    private DtoEntityMapper<LearningCircuitWithMembersDto, LearningCircuitEntity> circuitFullDtoMapper;
    private DtoEntityMapper<CircuitMemberStatusDto, RoomMemberStatusEntity> roomMemberStatusDtoMapper;
    private DtoEntityMapper<CircuitMemberStatusDto, CircuitMemberStatusEntity> circuitMemberStatusDtoMapper;


    private static final String DEFAULT_WTF_MESSAGE = "Started WTF";
    private static final String RESUMED_WTF_MESSAGE = "Resumed WTF";


    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circuitDtoMapper = mapperFactory.createDtoEntityMapper(LearningCircuitDto.class, LearningCircuitEntity.class);
        circuitFullDtoMapper = mapperFactory.createDtoEntityMapper(LearningCircuitWithMembersDto.class, LearningCircuitEntity.class);
        roomMemberStatusDtoMapper = mapperFactory.createDtoEntityMapper(CircuitMemberStatusDto.class, RoomMemberStatusEntity.class);

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

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        TalkRoomEntity wtfRoomEntity = new TalkRoomEntity();
        wtfRoomEntity.setId(UUID.randomUUID());
        wtfRoomEntity.setOrganizationId(organizationId);
        wtfRoomEntity.setRoomType(RoomType.WTF_ROOM);
        wtfRoomEntity.setRoomName(deriveWTFRoomName(learningCircuitEntity));

        talkRoomRepository.save(wtfRoomEntity);

        TalkRoomEntity statusRoomEntity = new TalkRoomEntity();
        statusRoomEntity.setId(UUID.randomUUID());
        statusRoomEntity.setOrganizationId(organizationId);
        statusRoomEntity.setRoomType(RoomType.STATUS_ROOM);
        statusRoomEntity.setRoomName(deriveStatusRoomName(learningCircuitEntity));

        talkRoomRepository.save(statusRoomEntity);

        //update circuit with the new room

        learningCircuitEntity.setWtfRoomId(wtfRoomEntity.getId());
        learningCircuitEntity.setStatusRoomId(statusRoomEntity.getId());
        learningCircuitEntity.setOpenTime(now);
        learningCircuitEntity.setCircuitState(CircuitState.TROUBLESHOOT);
        learningCircuitEntity.setSecondsBeforeOnHold(0L);
        learningCircuitRepository.save(learningCircuitEntity);

        //then I need to join this new person in the room...

        LearningCircuitMemberEntity circuitMemberEntity = new LearningCircuitMemberEntity();

        circuitMemberEntity.setId(UUID.randomUUID());
        circuitMemberEntity.setJoinTime(now);
        circuitMemberEntity.setCircuitId(learningCircuitEntity.getId());
        circuitMemberEntity.setOrganizationId(learningCircuitEntity.getOrganizationId());
        circuitMemberEntity.setMemberId(memberId);

        learningCircuitMemberRepository.save(circuitMemberEntity);

        addMemberToRoom(memberId, now, learningCircuitEntity, wtfRoomEntity);
        addMemberToRoom(memberId, now, learningCircuitEntity, statusRoomEntity);

        //then update active status

        activeWorkStatusManager.pushWTFStatus(organizationId, memberId, learningCircuitEntity.getId(), now, nanoTime);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_STARTED);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStarted(organizationId, memberId, now, nanoTime, circuitDto);

        return circuitDto;

    }

    private void addMemberToRoom(UUID memberId, LocalDateTime now, LearningCircuitEntity learningCircuitEntity, TalkRoomEntity roomEntity) {
        TalkRoomMemberEntity talkRoomMemberEntity = new TalkRoomMemberEntity();
        talkRoomMemberEntity.setId(UUID.randomUUID());
        talkRoomMemberEntity.setJoinTime(now);
        talkRoomMemberEntity.setRoomId(roomEntity.getId());
        talkRoomMemberEntity.setOrganizationId(learningCircuitEntity.getOrganizationId());
        talkRoomMemberEntity.setMemberId(memberId);

        talkRoomMemberRepository.save(talkRoomMemberEntity);

        talkRouter.joinRoom(learningCircuitEntity.getOrganizationId(), memberId, roomEntity.getId());
    }

    private void sendStatusMessageToCircuit(LearningCircuitEntity circuit, LocalDateTime now, Long nanoTime, CircuitMessageType messageType) {

        sendCircuitStatusMessage(circuit.getId(), circuit.getCircuitName(), circuit.getOwnerId(), now, nanoTime,
                circuit.getStatusRoomId(), messageType);
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
            log.warn("Member {} has multiple active circuits, should only be one", memberId);
            LearningCircuitEntity activeCircuit = activeCircuits.get(0);

            circuitDto = toDto(activeCircuit);

        }

        return circuitDto;
    }

    public LearningCircuitWithMembersDto getCircuitWithAllDetails(UUID organizationId, String circuitName) {

        log.info("inside getCircuitWithAllDetails");

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, circuitEntity);

        List<CircuitMemberStatusEntity> circuitParticipants = circuitMemberStatusRepository.findByCircuitId(circuitEntity.getId());
        List<RoomMemberStatusEntity> wtfMembers = roomMemberStatusRepository.findByRoomId(circuitEntity.getWtfRoomId());
        List<RoomMemberStatusEntity> retroMembers = roomMemberStatusRepository.findByRoomId(circuitEntity.getRetroRoomId());

        LearningCircuitWithMembersDto fullDto = toFullDetailsDto(circuitEntity);

        //TODO need to migrate data to this new display name column in the DB

        fullDto.setCircuitParticipants(circuitMemberStatusDtoMapper.toApiList(circuitParticipants));
        fullDto.setActiveWtfRoomMembers(roomMemberStatusDtoMapper.toApiList(wtfMembers));
        fullDto.setActiveRetroRoomMembers(roomMemberStatusDtoMapper.toApiList(retroMembers));

        return fullDto;
    }

    private LearningCircuitDto toDto(LearningCircuitEntity circuitEntity) {
        LearningCircuitDto circuitDto = circuitDtoMapper.toApi(circuitEntity);

        populateDtoFields(circuitDto, circuitEntity);

        return circuitDto;
    }

    private LearningCircuitWithMembersDto toFullDetailsDto(LearningCircuitEntity circuitEntity) {
        LearningCircuitWithMembersDto circuitDto = circuitFullDtoMapper.toApi(circuitEntity);

        populateDtoFields(circuitDto, circuitEntity);

        return circuitDto;
    }

    private void populateDtoFields(LearningCircuitDto circuitDto, LearningCircuitEntity circuitEntity) {
        if (circuitEntity != null) {
            if (circuitEntity.getWtfRoomId() != null) {
                circuitDto.setWtfTalkRoomId(circuitEntity.getWtfRoomId());
                circuitDto.setWtfTalkRoomName(deriveWTFRoomName(circuitEntity));

                circuitDto.setStatusTalkRoomId(circuitEntity.getStatusRoomId());
                circuitDto.setStatusTalkRoomName(deriveStatusRoomName(circuitEntity));
            }

            if (circuitEntity.getRetroRoomId() != null) {
                circuitDto.setRetroTalkRoomId(circuitEntity.getRetroRoomId());
                circuitDto.setRetroTalkRoomName(deriveRetroTalkRoomName(circuitEntity));
            }

            if (circuitEntity.getJsonTags() != null) {
                TagsInputDto tagsInput = JSONTransformer.fromJson(circuitEntity.getJsonTags(), TagsInputDto.class);
                circuitDto.setTags(tagsInput.getTags());
            }

            if (circuitEntity.getLastResumeTime() == null) {
                circuitDto.setStartTimerFromTime(circuitEntity.getOpenTime());
            } else {
                circuitDto.setStartTimerFromTime(circuitEntity.getLastResumeTime());
            }

            circuitDto.setStartTimerSecondsOffset(circuitEntity.getSecondsBeforeOnHold());
        }
    }

    private String deriveRetroTalkRoomName(LearningCircuitEntity activeCircuit) {
        return activeCircuit.getCircuitName() + RETRO_ROOM_SUFFIX;
    }

    private String deriveWTFRoomName(LearningCircuitEntity activeCircuit) {
        return activeCircuit.getCircuitName() + WTF_ROOM_SUFFIX;
    }

    private String deriveStatusRoomName(LearningCircuitEntity activeCircuit) {
        return activeCircuit.getCircuitName() + STATUS_ROOM_SUFFIX;
    }

    @Transactional
    public LearningCircuitDto startRetroForWTF(UUID organizationId, UUID memberId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        validateCircuitIsOwnedBy(memberId, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        if (learningCircuitEntity.getRetroRoomId() == null) {
            TalkRoomEntity retroRoomEntity = new TalkRoomEntity();
            retroRoomEntity.setId(UUID.randomUUID());
            retroRoomEntity.setOrganizationId(organizationId);
            retroRoomEntity.setRoomType(RoomType.RETRO_ROOM);
            retroRoomEntity.setRoomName(deriveRetroTalkRoomName(learningCircuitEntity));

            talkRoomRepository.save(retroRoomEntity);

            learningCircuitEntity.setRetroStartedTime(now);
            learningCircuitEntity.setRetroRoomId(retroRoomEntity.getId());

            learningCircuitRepository.save(learningCircuitEntity);
        }

        talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());
        talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getWtfRoomId());

        List<LearningCircuitMemberEntity> circuitMembers = learningCircuitMemberRepository.findByCircuitId(learningCircuitEntity.getId());

        talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getRetroRoomId());

        List<TalkRoomMemberEntity> retroRoomMembers = new ArrayList<>();

        for (LearningCircuitMemberEntity circuitMember : circuitMembers) {

            TalkRoomMemberEntity retroRoomMember = new TalkRoomMemberEntity();
            retroRoomMember.setId(UUID.randomUUID());
            retroRoomMember.setJoinTime(now);
            retroRoomMember.setRoomId(learningCircuitEntity.getRetroRoomId());
            retroRoomMember.setOrganizationId(organizationId);
            retroRoomMember.setMemberId(circuitMember.getMemberId());

            talkRouter.joinRoom(organizationId, circuitMember.getMemberId(), learningCircuitEntity.getRetroRoomId());

            retroRoomMembers.add(retroRoomMember);
        }

        talkRoomMemberRepository.save(retroRoomMembers);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_RETRO_STARTED);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfRetroStarted(organizationId, memberId, now, nanoTime, circuitDto);

        return circuitDto;

    }

    private void validateCircuitIsOwnedBy(UUID memberId, LearningCircuitEntity learningCircuitEntity) {

        if (!learningCircuitEntity.getOwnerId().equals(memberId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ACCESS_TO_CIRCUIT, "Retro can only be started by the owner of: " + learningCircuitEntity.getCircuitName());
        }
    }

    private void validateRetroNotAlreadyStarted(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getRetroRoomId() != null) {
            throw new ConflictException(ConflictErrorCodes.RETRO_ALREADY_STARTED, "Retro already started for circuit: " + circuitName);
        }
    }

    private void validateCircuitIsActive(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitState() != CircuitState.TROUBLESHOOT) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active: " + circuitName);
        }
    }

    private void validateCircuitIsActiveOrOnHold(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (!(learningCircuitEntity.getCircuitState() == CircuitState.TROUBLESHOOT
                || learningCircuitEntity.getCircuitState() == CircuitState.ONHOLD)) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active or OnHold: " + circuitName);
        }
    }

    private void validateCircuitIsOnHold(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitState() != CircuitState.ONHOLD) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be OnHold: " + circuitName);
        }
    }

    private void validateCircuitIsSolvedOrRetro(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitState() != CircuitState.SOLVED && learningCircuitEntity.getCircuitState() != CircuitState.RETRO ) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Solved or in Retro: " + circuitName);
        }
    }

    private void validateCircuitExists(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find: " + circuitName);
        }
    }

    private void validateRoomIsFound(TalkRoomEntity roomEntity, String roomName) {
        if (roomEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ROOM, "Unable to find: " + roomName);
        }
    }

    private void validateMemberInRoom(UUID organizationId, UUID invokingMemberId, String talkRoomName) {
        log.info("org={}, member={}, room={}", organizationId, invokingMemberId, talkRoomName);

        TalkRoomMemberEntity foundRoomMember = talkRoomMemberRepository.findByOrganizationMemberAndTalkRoomId(organizationId, invokingMemberId, talkRoomName);
        if (foundRoomMember == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ACCESS_TO_CIRCUIT, "Unable to access talk room: " + talkRoomName);
        }
    }

    private void deleteRoomMember(UUID organizationId, UUID memberId, LocalDateTime leaveTime, UUID roomId) {

        TalkRoomMemberEntity roomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomId, memberId);

        if (roomMember != null) {
            talkRoomMemberRepository.delete(roomMember);
        }

    }


    public LearningCircuitDto solveWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_SOLVED);

        learningCircuitEntity.setCloseTime(now);
        learningCircuitEntity.setCircuitState(CircuitState.SOLVED);

        learningCircuitRepository.save(learningCircuitEntity);

        activeWorkStatusManager.resolveWTFWithYay(organizationId, ownerId, now, nanoTime);

        //retro room is still open

        talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());

        talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getWtfRoomId());

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStopped(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;
    }


    public LearningCircuitDto abortWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActiveOrOnHold(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_ABORTED);

        learningCircuitEntity.setCloseTime(now);
        learningCircuitEntity.setCircuitState(CircuitState.ABORTED);

        learningCircuitRepository.save(learningCircuitEntity);

        activeWorkStatusManager.resolveWTFWithAbort(organizationId, ownerId, now, nanoTime);

        talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());

        talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getWtfRoomId());

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStopped(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;

    }

    @Transactional
    public LearningCircuitDto putWTFOnHoldWithDoItLater(UUID organizationId, UUID ownerId, String circuitName) {
        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        //every time I put it on hold, I calculate the seconds before on hold
        long durationInSeconds = calculateSecondsBeforeOnHold(learningCircuitEntity, now);
        learningCircuitEntity.setSecondsBeforeOnHold(durationInSeconds);

        learningCircuitEntity.setLastOnHoldTime(now);
        learningCircuitEntity.setLastResumeTime(null);
        learningCircuitEntity.setCircuitState(CircuitState.ONHOLD);

        learningCircuitRepository.save(learningCircuitEntity);

        if (learningCircuitEntity.getWtfRoomId() != null) {
            talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getWtfRoomId());

            talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getWtfRoomId());
        }

        if (learningCircuitEntity.getStatusRoomId() != null) {
            talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getStatusRoomId());

            talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getStatusRoomId());
        }

        if (learningCircuitEntity.getRetroRoomId() != null) {
            talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getRetroRoomId());

            talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getRetroRoomId());
        }

        activeWorkStatusManager.resolveWTFWithAbort(organizationId, ownerId, now, nanoTime);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_ONHOLD);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStopped(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;
    }

    public LearningCircuitDto resumeCircuit(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsOnHold(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        learningCircuitEntity.setLastResumeTime(now);
        learningCircuitEntity.setLastOnHoldTime(null);

        learningCircuitEntity.setCircuitState(CircuitState.TROUBLESHOOT);

        learningCircuitRepository.save(learningCircuitEntity);

        if (learningCircuitEntity.getWtfRoomId() != null ) {
            reviveRoom(now, learningCircuitEntity, learningCircuitEntity.getWtfRoomId());
        }

        if (learningCircuitEntity.getStatusRoomId() != null) {
            reviveRoom(now, learningCircuitEntity, learningCircuitEntity.getStatusRoomId());
        }

        activeWorkStatusManager.pushWTFStatus(organizationId, ownerId, learningCircuitEntity.getId(), now, nanoTime);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_RESUMED);

        teamCircuitOperator.notifyTeamOfWTFResumed(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;
    }

    private void reviveRoom(LocalDateTime now, LearningCircuitEntity learningCircuitEntity, UUID roomId) {

        List<LearningCircuitMemberEntity> circuitMembers = learningCircuitMemberRepository.findByCircuitId(learningCircuitEntity.getId());

        talkRoomMemberRepository.deleteMembersInRoom(roomId);

        List<TalkRoomMemberEntity> roomMembers = new ArrayList<>();

        for (LearningCircuitMemberEntity circuitMember : circuitMembers) {

            TalkRoomMemberEntity roomMember = new TalkRoomMemberEntity();
            roomMember.setId(UUID.randomUUID());
            roomMember.setJoinTime(now);
            roomMember.setRoomId(roomId);
            roomMember.setOrganizationId(learningCircuitEntity.getOrganizationId());
            roomMember.setMemberId(circuitMember.getMemberId());

            talkRouter.joinRoom(learningCircuitEntity.getOrganizationId(), circuitMember.getMemberId(), roomId);

            roomMembers.add(roomMember);
        }

        talkRoomMemberRepository.save(roomMembers);

        talkRouter.reviveRoom(learningCircuitEntity.getOrganizationId(), roomId);
    }

    public LearningCircuitDto reopenSolvedWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsSolvedOrRetro(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();


        if (learningCircuitEntity.getCircuitState() == CircuitState.RETRO) {

            talkRouter.closeRoom(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getRetroRoomId());

            talkRoomMemberRepository.deleteMembersInRoom(learningCircuitEntity.getRetroRoomId());

            learningCircuitEntity.setRetroStartedTime(null);
        }

        long durationInSeconds = calculateSecondsBeforeOnHold(learningCircuitEntity, learningCircuitEntity.getCloseTime());
        learningCircuitEntity.setSecondsBeforeOnHold(durationInSeconds);

        learningCircuitEntity.setLastOnHoldTime(learningCircuitEntity.getCloseTime());
        learningCircuitEntity.setLastResumeTime(now);
        learningCircuitEntity.setCloseTime(null);

        learningCircuitEntity.setCircuitState(CircuitState.TROUBLESHOOT);

        learningCircuitRepository.save(learningCircuitEntity);

        reviveRoom(now, learningCircuitEntity, learningCircuitEntity.getWtfRoomId());

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFResumed(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;
    }

    public LearningCircuitDto closeWTF(UUID organizationId, UUID id, String circuitName) {
        return null;
    }

    public TalkMessageDto joinRoom(UUID organizationId, UUID memberId, String roomName) {

        //another person joining a room, should add that person as a member.

        TalkRoomEntity roomEntity = talkRoomRepository.findByOrganizationIdAndRoomName(organizationId, roomName);

        validateRoomIsFound(roomEntity, roomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity == null) {
            roomMemberEntity = new TalkRoomMemberEntity();
            roomMemberEntity.setId(UUID.randomUUID());
            roomMemberEntity.setRoomId(roomEntity.getId());
            roomMemberEntity.setOrganizationId(organizationId);
            roomMemberEntity.setMemberId(memberId);
            roomMemberEntity.setJoinTime(now);

            talkRoomMemberRepository.save(roomMemberEntity);

        }

        talkRouter.joinRoom(organizationId, memberId, roomEntity.getId());

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findCircuitByOrganizationAndRoomName(organizationId, roomName);

        return sendRoomStatusMessage(circuitEntity.getOwnerId(), memberId, now, nanoTime, roomEntity.getId(), CircuitMessageType.ROOM_MEMBER_JOIN);
    }


    public TalkMessageDto leaveRoom(UUID organizationId, UUID memberId, String roomName) {

        TalkRoomEntity roomEntity = talkRoomRepository.findByOrganizationIdAndRoomName(organizationId, roomName);

        validateRoomIsFound(roomEntity, roomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity != null) {

            talkRouter.leaveRoom(organizationId, memberId, roomEntity.getId());

            talkRoomMemberRepository.delete(roomMemberEntity);
        }

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findCircuitByOrganizationAndRoomName(organizationId, roomName);

        validateCircuitExists("Circuit for room "+roomName, circuitEntity);

        return sendRoomStatusMessage(circuitEntity.getOwnerId(), memberId, now, nanoTime, roomEntity.getId(), CircuitMessageType.ROOM_MEMBER_JOIN);
    }

    private long calculateSecondsBeforeOnHold(LearningCircuitEntity circuitEntity, LocalDateTime endTime) {
        long totalDuration = 0;

        if (circuitEntity.getSecondsBeforeOnHold() != null) {
            totalDuration = circuitEntity.getSecondsBeforeOnHold();
        }

        //either take the additional time from start, or from resume
        if (circuitEntity.getLastResumeTime() == null) {
            long additionalDuration = ChronoUnit.SECONDS.between(circuitEntity.getOpenTime(), endTime);
            totalDuration += additionalDuration;
        } else {
            long additionalDuration = ChronoUnit.SECONDS.between(circuitEntity.getLastResumeTime(), endTime);
            totalDuration += additionalDuration;
        }

        return totalDuration;
    }


    public TalkMessageDto publishChatToTalkRoom(UUID organizationId, UUID fromMemberId, String talkRoomName, String chatMessage) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findCircuitByOrganizationAndRoomName(organizationId, talkRoomName);

        validateCircuitExists(talkRoomName, learningCircuitEntity);
        validateCircuitIsActive(talkRoomName, learningCircuitEntity);

        validateMemberInRoom(organizationId, fromMemberId, talkRoomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();
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

    private TalkMessageDto sendRoomStatusMessage(UUID circuitOwnerId, UUID memberId, LocalDateTime now, Long nanoTime, UUID roomId, CircuitMessageType messageType) {

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

            LocalDateTime now = gridClock.now();
            Long nanoTime = gridClock.nanoTime();

            List<LearningCircuitRoomEntity> circuitRooms = learningCircuitRoomRepository.findRoomsByMembership(memberConnection.getOrganizationId(), memberConnection.getMemberId());

            for (LearningCircuitRoomEntity circuitRoom : circuitRooms) {

                deleteRoomMember(memberConnection.getOrganizationId(), memberConnection.getMemberId(), gridClock.now(), circuitRoom.getRoomId());

                sendRoomStatusMessage(circuitRoom.getCircuitOwnerId(), memberConnection.getMemberId(), now, nanoTime, circuitRoom.getRoomId(), CircuitMessageType.ROOM_MEMBER_INACTIVE);
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

        dictionaryCapability.touchBlankDefinitions(organizationId, ownerId, tagsInputDto.getTags());

        return toDto(circuitEntity);
    }

    public TalkMessageDto publishSnippetToActiveCircuit(UUID organizationId, UUID memberId, NewSnippetEventDto newSnippetEventDto) {
        LearningCircuitDto learningCircuitDto = getMyActiveWTFCircuit(organizationId, memberId);

        TalkMessageDto messageDto = null;
        if (learningCircuitDto != null) {

            LocalDateTime now = gridClock.now();
            Long nanoTime = gridClock.nanoTime();
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

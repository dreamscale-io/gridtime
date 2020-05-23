package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.api.circuit.CircuitStatusDto;
import com.dreamscale.gridtime.core.capability.directory.DictionaryCapability;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberStatusEntity;
import com.dreamscale.gridtime.core.domain.member.MemberStatusRepository;
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
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.GridClock;
import com.dreamscale.gridtime.core.service.MemberDetailsService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WTFCircuitOperator {

    public static final String ROOM_URN_PREFIX = "/talk/to/room/";
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

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private ActiveJoinCircuitRepository activeJoinCircuitRepository;

    @Autowired
    private RoomOperator roomOperator;


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
        circuitMemberStatusDtoMapper = mapperFactory.createDtoEntityMapper(CircuitMemberStatusDto.class, CircuitMemberStatusEntity.class);

        sillyNameGenerator = new SillyNameGenerator();
    }

    @Transactional
    public LearningCircuitDto startWTF(UUID organizationId, UUID memberId) {
        String circuitName = sillyNameGenerator.random();

        log.info("[WTFCircuitOperator] Creating new circuit : " + circuitName);
        return startWTFWithCustomName(organizationId, memberId, circuitName);
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

    public LearningCircuitDto startWTFWithCustomName(UUID organizationId, UUID memberId, String circuitName) {

        validateNoActiveCircuit(organizationId, memberId);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();


        LearningCircuitEntity learningCircuitEntity = createLearningCircuit(now, nanoTime, circuitName, organizationId, memberId);

        //addMemberToRoom(memberId, now, learningCircuitEntity, wtfRoomEntity);

        //then update active status

        activeWorkStatusManager.pushWTFStatus(organizationId, memberId, learningCircuitEntity.getId(), now, nanoTime);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_STARTED);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStarted(organizationId, memberId, now, nanoTime, circuitDto);

        return circuitDto;

    }

    private LearningCircuitEntity createLearningCircuit(LocalDateTime now, Long nanoTime, String circuitName, UUID organizationId, UUID memberId) {
        LearningCircuitEntity learningCircuitEntity = new LearningCircuitEntity();
        learningCircuitEntity.setId(UUID.randomUUID());
        learningCircuitEntity.setCircuitName(circuitName);
        learningCircuitEntity.setOrganizationId(organizationId);
        learningCircuitEntity.setOwnerId(memberId);
        learningCircuitEntity.setModeratorId(memberId);

        learningCircuitEntity = tryToSaveAndReserveName(learningCircuitEntity);

        //so now I've got a reserved room Id, for my circuit, my wtf room name will automatically be circuit_name/wtf


        log.debug("[WTFCircuitOperator] Creating WTF circuit {} at {}", circuitName, nanoTime);

        TalkRoomEntity wtfRoomEntity = new TalkRoomEntity();
        wtfRoomEntity.setId(UUID.randomUUID());
        wtfRoomEntity.setOrganizationId(organizationId);
        wtfRoomEntity.setRoomType(RoomType.WTF_ROOM);
        wtfRoomEntity.setRoomName(deriveWTFRoomName(learningCircuitEntity));

        talkRoomRepository.save(wtfRoomEntity);

        //update circuit with the new room

        learningCircuitEntity.setWtfRoomId(wtfRoomEntity.getId());
        learningCircuitEntity.setOpenTime(now);
        learningCircuitEntity.setWtfOpenNanoTime(nanoTime);
        learningCircuitEntity.setCircuitState(LearningCircuitState.TROUBLESHOOT);
        learningCircuitEntity.setTotalCircuitElapsedNanoTime(0L);
        learningCircuitEntity.setTotalCircuitPausedNanoTime(0L);

        learningCircuitRepository.save(learningCircuitEntity);

        //then I need to join this new person in the room...

        log.debug("[WTFCircuitOperator] Member {} joining circuit {}", memberId, learningCircuitEntity.getCircuitName());

        LearningCircuitMemberEntity circuitMemberEntity = new LearningCircuitMemberEntity();

        circuitMemberEntity.setId(UUID.randomUUID());
        circuitMemberEntity.setJoinTime(now);
        circuitMemberEntity.setCircuitId(learningCircuitEntity.getId());
        circuitMemberEntity.setOrganizationId(learningCircuitEntity.getOrganizationId());
        circuitMemberEntity.setMemberId(memberId);

        learningCircuitMemberRepository.save(circuitMemberEntity);

        pauseExistingWTFIfDifferentCircuit(now, nanoTime, organizationId, memberId, learningCircuitEntity.getId());
        updateActiveJoinedCircuit(now, organizationId, memberId, learningCircuitEntity, JoinType.OWNER);
        return learningCircuitEntity;
    }

    private void validateNoActiveCircuit(UUID organizationId, UUID memberId) {
        MemberStatusEntity memberStatusEntity = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);

        if (memberStatusEntity != null && memberStatusEntity.getActiveCircuitId() != null) {
            throw new ConflictException(ConflictErrorCodes.CONFLICTING_ACTIVE_CIRCUIT, "User already has an active circuit.");
        }
    }

    @Transactional
    void sendStatusMessageToCircuit(LearningCircuitEntity circuit, LocalDateTime now, Long nanoTime, CircuitMessageType messageType) {

        UUID roomId = null;
        String urn = null;

        if (circuit.getCircuitState() == LearningCircuitState.TROUBLESHOOT) {
            roomId = circuit.getWtfRoomId();
            urn = ROOM_URN_PREFIX + deriveWTFRoomName(circuit);
        } else if (circuit.getCircuitState() == LearningCircuitState.RETRO) {
            roomId = circuit.getRetroRoomId();
            urn = ROOM_URN_PREFIX + deriveRetroTalkRoomName(circuit);
        }

        if (roomId != null) {
            sendCircuitStatusMessage(urn, circuit.getId(), circuit.getCircuitName(), circuit.getOwnerId(), now, nanoTime,
                    roomId, messageType);
        }
    }


    public LearningCircuitDto getCircuit(UUID organizationId, UUID circuitId) {

        LearningCircuitEntity circuitEntity = learningCircuitRepository.findByOrganizationIdAndId(organizationId, circuitId);

        return toDto(circuitEntity);
    }


    public LearningCircuitDto getMyActiveWTFCircuit(UUID organizationId, UUID memberId) {

        ActiveJoinCircuitEntity activeJoinCircuit = activeJoinCircuitRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        LearningCircuitDto circuitDto = null;

        if (activeJoinCircuit != null && activeJoinCircuit.getJoinedCircuitId() != null) {

            circuitDto = getCircuit(organizationId, activeJoinCircuit.getJoinedCircuitId());
        }

        return circuitDto;
    }

    public LearningCircuitWithMembersDto getCircuitWithAllDetails(UUID organizationId, String circuitName) {

        log.info("[WTFCircuitOperator] getCircuitWithAllDetails");

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
            }

            if (circuitEntity.getRetroRoomId() != null) {
                circuitDto.setRetroTalkRoomId(circuitEntity.getRetroRoomId());
                circuitDto.setRetroTalkRoomName(deriveRetroTalkRoomName(circuitEntity));
            }

            if (circuitEntity.getJsonTags() != null) {
                TagsInputDto tagsInput = JSONTransformer.fromJson(circuitEntity.getJsonTags(), TagsInputDto.class);
                circuitDto.setTags(tagsInput.getTags());
            }

            String ownerName = memberDetailsService.lookupMemberName(circuitEntity.getOrganizationId(), circuitEntity.getOwnerId());
            circuitDto.setOwnerName(ownerName);

            String moderatorName = memberDetailsService.lookupMemberName(circuitEntity.getOrganizationId(), circuitEntity.getModeratorId());
            circuitDto.setModeratorName(moderatorName);
        }
    }

    private String deriveRetroTalkRoomName(LearningCircuitEntity circuit) {
        return circuit.getCircuitName() + RETRO_ROOM_SUFFIX;
    }

    private String deriveWTFRoomName(LearningCircuitEntity circuit) {
        return circuit.getCircuitName() + WTF_ROOM_SUFFIX;
    }

    @Transactional
    public LearningCircuitDto startRetroForWTF(UUID organizationId, UUID memberId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsOwnedBy(memberId, learningCircuitEntity);
        validateCircuitIsActiveOrSolved(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[WTFCircuitOperator] Starting Retro for WTF circuit {} at {}", circuitName, nanoTime);

        pauseExistingWTFIfDifferentCircuit(now, nanoTime, organizationId, memberId, learningCircuitEntity.getId());
        updateActiveJoinedCircuit(now, organizationId, memberId, learningCircuitEntity, JoinType.OWNER);

        if (learningCircuitEntity.getCircuitState() == LearningCircuitState.TROUBLESHOOT) {

            long nanoElapsedTime = calculateActiveNanoElapsedTime(learningCircuitEntity, nanoTime);
            learningCircuitEntity.setTotalCircuitElapsedNanoTime(nanoElapsedTime);

            learningCircuitEntity.setSolvedCircuitNanoTime(nanoTime);
        }

        if (learningCircuitEntity.getRetroRoomId() == null) {
            TalkRoomEntity retroRoomEntity = new TalkRoomEntity();
            retroRoomEntity.setId(UUID.randomUUID());
            retroRoomEntity.setOrganizationId(organizationId);
            retroRoomEntity.setRoomType(RoomType.RETRO_ROOM);
            retroRoomEntity.setRoomName(deriveRetroTalkRoomName(learningCircuitEntity));

            talkRoomRepository.save(retroRoomEntity);

            learningCircuitEntity.setRetroRoomId(retroRoomEntity.getId());
        }

        learningCircuitEntity.setCircuitState(LearningCircuitState.RETRO);
        learningCircuitEntity.setRetroOpenNanoTime(nanoTime);

        learningCircuitRepository.save(learningCircuitEntity);

        //TODO do we want to notify this set of people of the retro starting?
        List<LearningCircuitMemberEntity> circuitMembers = learningCircuitMemberRepository.findByCircuitId(learningCircuitEntity.getId());

        //yo, retro is starting would you like to join?

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_RETRO_STARTED);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfRetroStarted(organizationId, memberId, now, nanoTime, circuitDto);

        activeWorkStatusManager.pushWTFStatus(organizationId, memberId, learningCircuitEntity.getId(), now, nanoTime);

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
        if (learningCircuitEntity.getCircuitState() != LearningCircuitState.TROUBLESHOOT) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active: " + circuitName);
        }
    }

    private void validateCircuitIsJoinable(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitState() != LearningCircuitState.TROUBLESHOOT && learningCircuitEntity.getCircuitState() != LearningCircuitState.RETRO) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be in Troubleshoot or Retro state to be joinable, Circuit: " + circuitName);
        }
    }

    private void validateCircuitIsActiveOrOnHold(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (!(learningCircuitEntity.getCircuitState() == LearningCircuitState.TROUBLESHOOT
                || learningCircuitEntity.getCircuitState() == LearningCircuitState.ONHOLD)) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active or OnHold: " + circuitName);
        }
    }

    private void validateCircuitIsActiveOrSolved(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (!(learningCircuitEntity.getCircuitState() == LearningCircuitState.TROUBLESHOOT
                || learningCircuitEntity.getCircuitState() == LearningCircuitState.SOLVED)) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Active or Solved: " + circuitName);
        }
    }

    private void validateCircuitIsOnHold(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitState() != LearningCircuitState.ONHOLD) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be OnHold: " + circuitName);
        }
    }

    private void validateCircuitIsSolvedOrRetro(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity.getCircuitState() != LearningCircuitState.SOLVED && learningCircuitEntity.getCircuitState() != LearningCircuitState.RETRO) {
            throw new ConflictException(ConflictErrorCodes.CIRCUIT_IN_WRONG_STATE, "Circuit must be Solved or in Retro: " + circuitName);
        }
    }

    private void validateCircuitExists(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find: " + circuitName);
        }
    }

    private void validateMemberIsCircuitParticipant(LearningCircuitEntity circuit, UUID invokingMemberId) {
        log.debug("[WTFCircuitOperator] validate org={}, member={}, circuit={}", circuit.getOrganizationId(), invokingMemberId, circuit.getCircuitName());

        LearningCircuitMemberEntity foundRoomMember = learningCircuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(circuit.getOrganizationId(), circuit.getId(), invokingMemberId);
        if (foundRoomMember == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ACCESS_TO_CIRCUIT, "Member " + invokingMemberId + " unable to access circuit: " + circuit.getCircuitName());
        }
    }


    @Transactional
    public LearningCircuitDto solveWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[WTFCircuitOperator] Solving WTF circuit {} at {}", circuitName, nanoTime);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_SOLVED);

        long nanoElapsedTime = calculateActiveNanoElapsedTime(learningCircuitEntity, nanoTime);
        learningCircuitEntity.setTotalCircuitElapsedNanoTime(nanoElapsedTime);

        learningCircuitEntity.setSolvedCircuitNanoTime(nanoTime);

        learningCircuitEntity.setCircuitState(LearningCircuitState.SOLVED);

        learningCircuitRepository.save(learningCircuitEntity);

        activeWorkStatusManager.resolveWTFWithYay(organizationId, ownerId, now, nanoTime);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStopped(organizationId, ownerId, now, nanoTime, circuitDto);

        clearActiveJoinedCircuit(organizationId, ownerId);

        return circuitDto;
    }

    public LearningCircuitDto cancelWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);

        cancelWTFAndCommit(organizationId, ownerId, circuitName, learningCircuitEntity, now, nanoTime);

        activeWorkStatusManager.resolveWTFWithCancel(organizationId, ownerId, now, nanoTime);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_CANCELED);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFStopped(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;

    }

    @Transactional
    void cancelWTFAndCommit(UUID organizationId, UUID ownerId, String circuitName, LearningCircuitEntity learningCircuitEntity, LocalDateTime now, Long nanoTime) {
        log.debug("[WTFCircuitOperator] Cancel WTF circuit {} at {}", circuitName, nanoTime);

        if (learningCircuitEntity.getCircuitState() == LearningCircuitState.TROUBLESHOOT) {
            long nanoElapsedTime = calculateActiveNanoElapsedTime(learningCircuitEntity, nanoTime);
            learningCircuitEntity.setTotalCircuitElapsedNanoTime(nanoElapsedTime);
        }

        if (learningCircuitEntity.getCircuitState() == LearningCircuitState.ONHOLD) {
            long nanoElapsedTime = calculatePausedNanoElapsedTime(learningCircuitEntity, nanoTime);
            learningCircuitEntity.setTotalCircuitPausedNanoTime(nanoElapsedTime);
        }

        learningCircuitEntity.setCancelCircuitNanoTime(nanoTime);
        learningCircuitEntity.setCircuitState(LearningCircuitState.CANCELED);

        learningCircuitRepository.save(learningCircuitEntity);

        clearActiveJoinedCircuit(organizationId, ownerId);
    }

    @Transactional
    public LearningCircuitDto pauseWTFWithDoItLater(UUID organizationId, UUID ownerId, String circuitName) {
        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsActive(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[WTFCircuitOperator] Pause WTF circuit {} at {}", circuitName, nanoTime);
        return pauseAndUpdateCircuitStatus(now, nanoTime, learningCircuitEntity);
    }

    private LearningCircuitDto pauseAndUpdateCircuitStatus(LocalDateTime now, Long nanoTime, LearningCircuitEntity learningCircuitEntity) {
        //every time I put it on hold, I calculate the seconds before on hold
        long nanoElapsedTime = calculateActiveNanoElapsedTime(learningCircuitEntity, nanoTime);

        learningCircuitEntity.setTotalCircuitElapsedNanoTime(nanoElapsedTime);
        learningCircuitEntity.setPauseCircuitNanoTime(nanoTime);
        learningCircuitEntity.setCircuitState(LearningCircuitState.ONHOLD);

        learningCircuitRepository.save(learningCircuitEntity);

        activeWorkStatusManager.resolveWTFWithCancel(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getOwnerId(), now, nanoTime);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_ONHOLD);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        clearActiveJoinedCircuit(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getOwnerId());

        teamCircuitOperator.notifyTeamOfWTFStopped(learningCircuitEntity.getOrganizationId(), learningCircuitEntity.getOwnerId(), now, nanoTime, circuitDto);

        return circuitDto;
    }

    //TODO start, cancel, pause


    @Transactional
    public LearningCircuitDto resumeWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsOnHold(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[WTFCircuitOperator] Resume WTF circuit {} at {}", circuitName, nanoTime);

        pauseExistingWTFIfDifferentCircuit(now, nanoTime, organizationId, ownerId, learningCircuitEntity.getId());
        updateActiveJoinedCircuit(now, organizationId, ownerId, learningCircuitEntity, JoinType.OWNER);

        //every time I resume a circuit, calculate how long I've been paused
        long nanoElapsedTime = calculatePausedNanoElapsedTime(learningCircuitEntity, nanoTime);

        learningCircuitEntity.setTotalCircuitPausedNanoTime(nanoElapsedTime);
        learningCircuitEntity.setResumeCircuitNanoTime(nanoTime);

        learningCircuitEntity.setCircuitState(LearningCircuitState.TROUBLESHOOT);

        learningCircuitRepository.save(learningCircuitEntity);

        activeWorkStatusManager.pushWTFStatus(organizationId, ownerId, learningCircuitEntity.getId(), now, nanoTime);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        sendStatusMessageToCircuit(learningCircuitEntity, now, nanoTime, CircuitMessageType.WTF_RESUMED);

        teamCircuitOperator.notifyTeamOfWTFResumed(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;
    }


    @Transactional
    public LearningCircuitDto reopenSolvedWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsSolvedOrRetro(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[WTFCircuitOperator] Reopen WTF circuit {} at {}", circuitName, nanoTime);

        pauseExistingWTFIfDifferentCircuit(now, nanoTime, organizationId, ownerId, learningCircuitEntity.getId());
        updateActiveJoinedCircuit(now, organizationId, ownerId, learningCircuitEntity, JoinType.OWNER);

        if (learningCircuitEntity.getCircuitState() == LearningCircuitState.RETRO) {

            learningCircuitEntity.setRetroOpenNanoTime(null);
        }

        //so if we are re-opening, then whenever we solved this thing prior, treat that duration as a pause, and this as a resume

        learningCircuitEntity.setPauseCircuitNanoTime(learningCircuitEntity.getSolvedCircuitNanoTime());
        long durationInSeconds = calculatePausedNanoElapsedTime(learningCircuitEntity, nanoTime);
        learningCircuitEntity.setTotalCircuitElapsedNanoTime(durationInSeconds);

        learningCircuitEntity.setResumeCircuitNanoTime(nanoTime);
        learningCircuitEntity.setSolvedCircuitNanoTime(null);
        learningCircuitEntity.setCircuitState(LearningCircuitState.TROUBLESHOOT);

        learningCircuitRepository.save(learningCircuitEntity);

        LearningCircuitDto circuitDto = toDto(learningCircuitEntity);

        teamCircuitOperator.notifyTeamOfWTFResumed(organizationId, ownerId, now, nanoTime, circuitDto);

        return circuitDto;
    }

    @Transactional
    public LearningCircuitDto closeWTF(UUID organizationId, UUID ownerId, String circuitName) {

        LearningCircuitEntity learningCircuitEntity = learningCircuitRepository.findByOrganizationIdAndOwnerIdAndCircuitName(organizationId, ownerId, circuitName);

        validateCircuitExists(circuitName, learningCircuitEntity);
        validateCircuitIsSolvedOrRetro(circuitName, learningCircuitEntity);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        learningCircuitEntity.setCloseCircuitNanoTime(nanoTime);
        learningCircuitEntity.setCircuitState(LearningCircuitState.CLOSED);

        learningCircuitRepository.save(learningCircuitEntity);

        clearActiveJoinedCircuit(organizationId, ownerId);

        return toDto(learningCircuitEntity);
    }

    @Transactional
    public LearningCircuitDto joinWTF(UUID organizationId, UUID memberId, String circuitName) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        LearningCircuitEntity wtfCircuit = learningCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, circuitName);

        validateCircuitExists(circuitName, wtfCircuit);
        validateCircuitIsJoinable(circuitName, wtfCircuit);

        //if we own it, we should already be setup as a member, so this is a no-op

        if (!wtfCircuit.getOwnerId().equals(memberId)) {

            pauseExistingWTFIfDifferentCircuit(now, nanoTime, organizationId, memberId, wtfCircuit.getId());
            updateActiveJoinedCircuit(now, organizationId, memberId, wtfCircuit, JoinType.TEAM_MEMBER_JOIN);

            joinCircuitAndSendRoomStatusUpdate(now, nanoTime, organizationId, memberId, wtfCircuit);
        }

        return toDto(wtfCircuit);

    }

    private void joinCircuitAndSendRoomStatusUpdate(LocalDateTime now, Long nanoTime, UUID organizationId, UUID memberId, LearningCircuitEntity wtfCircuit) {

        LearningCircuitMemberEntity circuitMember = learningCircuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(organizationId, wtfCircuit.getId(), memberId);

        if (circuitMember == null) {

            log.debug("[WTFCircuitOperator] Member {} joining circuit {}", memberId, wtfCircuit.getCircuitName());

            circuitMember = new LearningCircuitMemberEntity();
            circuitMember.setId(UUID.randomUUID());
            circuitMember.setCircuitId(wtfCircuit.getId());
            circuitMember.setOrganizationId(organizationId);
            circuitMember.setMemberId(memberId);
            circuitMember.setJoinTime(now);

            learningCircuitMemberRepository.save(circuitMember);

        }
        //if circuit is in troubleshoot state, join the troubleshooting room.

        if (wtfCircuit.getCircuitState() == LearningCircuitState.TROUBLESHOOT) {
            String urn = ROOM_URN_PREFIX + deriveWTFRoomName(wtfCircuit);
            sendRoomStatusMessage(urn, wtfCircuit.getOwnerId(), memberId, now, nanoTime,
                    wtfCircuit.getWtfRoomId(), CircuitMessageType.ROOM_MEMBER_JOIN);
        } else if (wtfCircuit.getCircuitState() == LearningCircuitState.RETRO) {
            String urn = ROOM_URN_PREFIX + deriveRetroTalkRoomName(wtfCircuit);
            sendRoomStatusMessage(urn, wtfCircuit.getOwnerId(), memberId, now, nanoTime,
                    wtfCircuit.getRetroRoomId(), CircuitMessageType.ROOM_MEMBER_JOIN);
        }
    }

    private void pauseExistingWTFIfDifferentCircuit(LocalDateTime now, Long nanoTime, UUID organizationId, UUID memberId, UUID circuitId) {

        ActiveJoinCircuitEntity existingJoinCircuit = activeJoinCircuitRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        if (existingJoinCircuit != null && existingJoinCircuit.getJoinType() == JoinType.OWNER &&
                existingJoinCircuit.getJoinedCircuitId() != null && !existingJoinCircuit.getJoinedCircuitId().equals(circuitId)) {

            LearningCircuitEntity oldCircuit = learningCircuitRepository.findByOrganizationIdAndId(organizationId, existingJoinCircuit.getJoinedCircuitId());
            pauseAndUpdateCircuitStatus(now, nanoTime, oldCircuit);
        }
    }

    private void updateActiveJoinedCircuit(LocalDateTime now, UUID organizationId, UUID memberId, LearningCircuitEntity newCircuit, JoinType joinType) {

        ActiveJoinCircuitEntity existingJoinCircuit = activeJoinCircuitRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        if (existingJoinCircuit == null) {
            existingJoinCircuit = new ActiveJoinCircuitEntity();
            existingJoinCircuit.setId(UUID.randomUUID());
            existingJoinCircuit.setOrganizationId(organizationId);
            existingJoinCircuit.setMemberId(memberId);
        }

        existingJoinCircuit.setJoinedCircuitId(newCircuit.getId());
        existingJoinCircuit.setJoinedCircuitOwnerId(newCircuit.getOwnerId());
        existingJoinCircuit.setJoinDate(now);
        existingJoinCircuit.setJoinedCircuitType(JoinedCircuitType.WTF);
        existingJoinCircuit.setJoinType(joinType);

        activeJoinCircuitRepository.save(existingJoinCircuit);

    }

    private void clearActiveJoinedCircuit(UUID organizationId, UUID memberId) {

        ActiveJoinCircuitEntity existingJoinCircuit = activeJoinCircuitRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        //okay now, we can join this new WTF circuit.

        if (existingJoinCircuit == null) {
            existingJoinCircuit = new ActiveJoinCircuitEntity();
            existingJoinCircuit.setId(UUID.randomUUID());
            existingJoinCircuit.setOrganizationId(organizationId);
            existingJoinCircuit.setMemberId(memberId);
        }

        existingJoinCircuit.setJoinedCircuitId(null);
        existingJoinCircuit.setJoinedCircuitOwnerId(null);
        existingJoinCircuit.setJoinDate(null);
        existingJoinCircuit.setJoinedCircuitType(null);
        existingJoinCircuit.setJoinType(null);

        activeJoinCircuitRepository.save(existingJoinCircuit);

    }

    private long calculateActiveNanoElapsedTime(LearningCircuitEntity circuitEntity, Long nanoPauseTime) {
        long totalDuration = 0;

        if (circuitEntity.getTotalCircuitElapsedNanoTime() != null) {
            totalDuration = circuitEntity.getTotalCircuitElapsedNanoTime();
        }

        //either take the additional time from start, or from resume
        if (circuitEntity.getResumeCircuitNanoTime() == null) {
            long additionalDuration = nanoPauseTime - circuitEntity.getWtfOpenNanoTime();
            totalDuration += additionalDuration;
        } else {
            long additionalDuration = nanoPauseTime - circuitEntity.getResumeCircuitNanoTime();
            totalDuration += additionalDuration;
        }

        return totalDuration;
    }

    private long calculatePausedNanoElapsedTime(LearningCircuitEntity circuitEntity, Long nanoResumeTime) {
        long totalDuration = 0;

        if (circuitEntity.getTotalCircuitPausedNanoTime() != null) {
            totalDuration = circuitEntity.getTotalCircuitPausedNanoTime();
        }

        //from the time I paused, until now
        if (circuitEntity.getPauseCircuitNanoTime() != null) {
            long additionalDuration = nanoResumeTime - circuitEntity.getPauseCircuitNanoTime();
            totalDuration += additionalDuration;
        }

        return totalDuration;
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


    private void updateMemberStatusWithTouch(UUID roomId, UUID memberId) {
        //TODO can do this stuff later
        //activeStatusService.touchActivity(memberId);
    }

    private TalkMessageDto sendCircuitStatusMessage(String urn, UUID circuitId, String circuitName, UUID circuitOwnerId, LocalDateTime now, Long nanoTime, UUID roomId, CircuitMessageType messageType) {

        CircuitStatusDto statusDto = new CircuitStatusDto(circuitId, circuitName, messageType.name(), messageType.getStatusMessage());

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(circuitOwnerId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(statusDto));

        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        return talkMessageDto;
    }

    private TalkMessageDto sendRoomStatusMessage(String urn, UUID circuitOwnerId, UUID memberId, LocalDateTime now, Long nanoTime, UUID roomId, CircuitMessageType messageType) {

        RoomMemberStatusEventDto statusDto = new RoomMemberStatusEventDto(memberId, messageType.name(), messageType.getStatusMessage());

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(circuitOwnerId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(statusDto));
        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(roomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);

        return talkMessageDto;
    }


    private TalkMessageDto sendSnippetMessage(String urn, UUID messageId, LocalDateTime now, Long nanoTime, UUID fromMemberId, UUID roomId, NewSnippetEventDto snippet) {

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(messageId);
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(roomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.SNIPPET);
        messageEntity.setJsonBody(JSONTransformer.toJson(createSnippetMessage(snippet)));

        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(roomId, talkMessageDto);

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


    private String getRequestUriFromContext() {

        RequestContext context = RequestContext.get();

        if (context != null) {
            return context.getRequestUri();
        } else {
            return null;
        }
    }


    public void notifyRoomsOfMemberDisconnect(UUID oldConnectionId) {

        MemberConnectionEntity memberConnection = memberConnectionRepository.findByConnectionId(oldConnectionId);

        if (memberConnection != null) {

            LocalDateTime now = gridClock.now();
            Long nanoTime = gridClock.nanoTime();

            List<LearningCircuitRoomEntity> circuitRooms = learningCircuitRoomRepository.findRoomsByMembership(memberConnection.getOrganizationId(), memberConnection.getMemberId());

            for (LearningCircuitRoomEntity circuitRoom : circuitRooms) {

                String urn = circuitRoom.getRoomName();

                sendRoomStatusMessage(urn, circuitRoom.getCircuitOwnerId(), memberConnection.getMemberId(), now, nanoTime, circuitRoom.getRoomId(), CircuitMessageType.ROOM_MEMBER_OFFLINE);
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

            String urn = ROOM_URN_PREFIX + learningCircuitDto.getCircuitName() + WTF_ROOM_SUFFIX;

            messageDto = sendSnippetMessage(urn, messageId, now, nanoTime, memberId, learningCircuitDto.getWtfTalkRoomId(), newSnippetEventDto);
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


    public List<LearningCircuitDto> getAllParticipatingCircuitsForOtherMember(UUID organizationId, UUID id, UUID otherMemberId) {
        return null;
    }


}
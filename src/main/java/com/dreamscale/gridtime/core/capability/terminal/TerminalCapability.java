package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.*;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitCommandHistoryEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitCommandHistoryRepository;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitRepository;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.mapping.SillyNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TerminalCapability {

    @Autowired
    private TerminalRouteRegistry terminalRouteRegistry;


    @Autowired
    private TerminalCircuitRepository terminalCircuitRepository;

    @Autowired
    private TerminalCircuitCommandHistoryRepository terminalCircuitCommandHistoryRepository;

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private MapperFactory mapperFactory;

    @Autowired
    private RoomMemberStatusRepository roomMemberStatusRepository;

    @Autowired
    private EntityManager entityManager;

    private SillyNameGenerator sillyNameGenerator ;


    public static final String TERMINAL_ROOM_SUFFIX = "-term";

    private DtoEntityMapper<CircuitMemberStatusDto, RoomMemberStatusEntity> roomMemberStatusDtoMapper;
    private DtoEntityMapper<TerminalCircuitDto, TerminalCircuitEntity> circuitDtoMapper;


    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circuitDtoMapper = mapperFactory.createDtoEntityMapper(TerminalCircuitDto.class, TerminalCircuitEntity.class);
        roomMemberStatusDtoMapper = mapperFactory.createDtoEntityMapper(CircuitMemberStatusDto.class, RoomMemberStatusEntity.class);

        sillyNameGenerator = new SillyNameGenerator();
    }


    @Transactional
    public TerminalCircuitDto createCircuit(UUID organizationId, UUID invokingMemberId) {

        LocalDateTime now = gridClock.now();

        String terminalCircuitName = sillyNameGenerator.random();

        TerminalCircuitEntity circuit = new TerminalCircuitEntity();

        circuit.setId(UUID.randomUUID());
        circuit.setCircuitName(terminalCircuitName);
        circuit.setCreatedDate(now);
        circuit.setOrganizationId(organizationId);
        circuit.setCreatorId(invokingMemberId);

        tryToSaveAndReserveName(circuit);

        TalkRoomEntity room = createTalkRoomForCircuit(now, circuit);

        circuit.setTalkRoomId(room.getId());

        terminalCircuitRepository.save(circuit);

        entityManager.flush();

        RoomMemberStatusEntity creatorStatusEntity = roomMemberStatusRepository.findByRoomIdAndMemberId(room.getId(), invokingMemberId);

        CircuitMemberStatusDto creatorStatusDto = roomMemberStatusDtoMapper.toApi(creatorStatusEntity);

        TerminalCircuitDto circuitDto = circuitDtoMapper.toApi(circuit);

        circuitDto.setCreator(creatorStatusDto);
        circuitDto.setCircuitMembers(DefaultCollections.toList(creatorStatusDto));

        return circuitDto;
    }

    private TalkRoomEntity createTalkRoomForCircuit(LocalDateTime now, TerminalCircuitEntity circuit) {
        //create talk room for the circuit

        TalkRoomEntity room = new TalkRoomEntity();
        room.setId(UUID.randomUUID());
        room.setOrganizationId(circuit.getOrganizationId());
        room.setRoomName(deriveRoomName(circuit.getCircuitName()));
        room.setRoomType(RoomType.TERMINAL);

        talkRoomRepository.save(room);

        //join the member to the circuit as creator

        TalkRoomMemberEntity roomMember = new TalkRoomMemberEntity();
        roomMember.setId(UUID.randomUUID());
        roomMember.setOrganizationId(circuit.getOrganizationId());
        roomMember.setMemberId(circuit.getCreatorId());
        roomMember.setJoinTime(now);

        talkRoomMemberRepository.save(roomMember);
        return room;
    }

    private String deriveRoomName(String circuitName) {
        return circuitName + TERMINAL_ROOM_SUFFIX;
    }

    private TerminalCircuitEntity tryToSaveAndReserveName(TerminalCircuitEntity circuit) {

        TerminalCircuitEntity savedEntity = null;
        int retryCounter = 3;

        String requestedCircuitName = circuit.getCircuitName();

        while (savedEntity == null & retryCounter > 0)
            try {
                savedEntity = terminalCircuitRepository.save(circuit);
            } catch (Exception ex) {
                circuit.setCircuitName(sillyNameGenerator.randomNewExtension(requestedCircuitName));
                retryCounter--;
            }

        if (savedEntity == null) {
            throw new ConflictException(ConflictErrorCodes.CONFLICTING_CIRCUIT_NAME, "Unable to save Terminal Circuit with requested name after 3 tries: " + requestedCircuitName);
        }

        return savedEntity;

    }

    public TalkMessageDto runCommand(UUID organizationId, UUID invokingMemberId, String circuitName, CommandInputDto commandInputDto) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        validateNotNull("circuitName", circuitName);

        String lowerCaseName = circuitName.toLowerCase();
        TerminalCircuitEntity circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, lowerCaseName);

        validateCircuitExists(lowerCaseName, circuit);

        TerminalCircuitCommandHistoryEntity commandHistory = new TerminalCircuitCommandHistoryEntity();

        commandHistory.setId(UUID.randomUUID());
        commandHistory.setCircuitId(circuit.getId());
        commandHistory.setCommand(commandInputDto.getCommand());
        commandHistory.setArgs(StringUtils.join(commandInputDto.getArgs(), " "));
        commandHistory.setCommandDate(now);

        terminalCircuitCommandHistoryRepository.save(commandHistory);

        //need to look up circuit, validate exists

        //save command history

        //route the command

        //command and response should go out as talk messages

        //need to write a test with invalid circuit

        //need to clear the circuit tables in test setup

        //then deploy, and update the terminal client to create a new circuit before running all the commands

        return terminalRouteRegistry.routeCommand(organizationId, invokingMemberId, commandInputDto);
    }

    public SimpleStatusDto joinCircuit(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public SimpleStatusDto closeSession(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public TerminalCircuitDto getCircuit(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public SimpleStatusDto leaveCircuit(UUID organizationId, UUID invokingMemberId, String circuitName) {
        return null;
    }

    public SimpleStatusDto setEnvironmentParam(UUID organizationId, UUID invokingMemberId, String circuitName, EnvironmentParamInputDto environmentParamInputDto) {
        return null;
    }

    public CommandManualDto getManual(UUID organizationId, UUID memberId) {
        return terminalRouteRegistry.getManual(organizationId, memberId);
    }

    public CommandManualPageDto getManualPage(UUID organizationId, UUID memberId, Command command) {
        return terminalRouteRegistry.getManualPage(organizationId, memberId, command);
    }

    public CommandManualPageDto getManualPage(UUID organizationId, UUID memberId, ActivityContext activityContext) {
        return terminalRouteRegistry.getManualPage(organizationId, memberId, activityContext);
    }

    private void validateNotNull(String propertyName, Object property) {
        if (property == null) {
            throw new BadRequestException(ValidationErrorCodes.PROPERTY_CANT_BE_NULL, "Property " + propertyName + " cant be null");
        }
    }

    private void validateCircuitExists(String circuitName, TerminalCircuitEntity terminalCircuitEntity) {
        if (terminalCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find terminal circuit: " + circuitName);
        }
    }
}

package com.dreamscale.gridtime.core.capability.terminal;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.status.Status;
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
    private CircuitMemberRepository circuitMemberRepository;

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private MapperFactory mapperFactory;

    @Autowired
    private CircuitMemberStatusRepository circuitMemberStatusRepository;

    @Autowired
    private EntityManager entityManager;

    private SillyNameGenerator sillyNameGenerator ;


    public static final String TERMINAL_ROOM_SUFFIX = "-term";

    private DtoEntityMapper<CircuitMemberStatusDto, CircuitMemberStatusEntity> circuitMemberStatusDtoMapper;
    private DtoEntityMapper<TerminalCircuitDto, TerminalCircuitEntity> circuitDtoMapper;


    @PostConstruct
    private void init() throws IOException, URISyntaxException {
        circuitDtoMapper = mapperFactory.createDtoEntityMapper(TerminalCircuitDto.class, TerminalCircuitEntity.class);
        circuitMemberStatusDtoMapper = mapperFactory.createDtoEntityMapper(CircuitMemberStatusDto.class, CircuitMemberStatusEntity.class);

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

        CircuitMemberEntity circuitMember = new CircuitMemberEntity();
        circuitMember.setId(UUID.randomUUID());
        circuitMember.setCircuitId(circuit.getId());
        circuitMember.setOrganizationId(organizationId);
        circuitMember.setMemberId(invokingMemberId);
        circuitMember.setActiveInSession(true);
        circuitMember.setJoinTime(now);

        circuitMemberRepository.save(circuitMember);

        entityManager.flush();

        CircuitMemberStatusEntity creatorStatusEntity = circuitMemberStatusRepository.findByOrganizationIdAndCircuitIdAndMemberId(organizationId, circuit.getId(), invokingMemberId);

        CircuitMemberStatusDto creatorStatusDto = circuitMemberStatusDtoMapper.toApi(creatorStatusEntity);

        TerminalCircuitDto circuitDto = circuitDtoMapper.toApi(circuit);

        circuitDto.setCreator(creatorStatusDto);
        circuitDto.setCircuitMembers(DefaultCollections.toList(creatorStatusDto));

        return circuitDto;
    }

    private TalkRoomEntity createTalkRoomForCircuit(LocalDateTime now, TerminalCircuitEntity circuit) {

        TalkRoomEntity room = new TalkRoomEntity();
        room.setId(UUID.randomUUID());
        room.setOrganizationId(circuit.getOrganizationId());
        room.setRoomName(deriveRoomName(circuit.getCircuitName()));
        room.setRoomType(RoomType.TERMINAL);

        talkRoomRepository.save(room);

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

        validateNotNull("circuitName", circuitName);

        String lowerCaseName = circuitName.toLowerCase();
        TerminalCircuitEntity circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, lowerCaseName);

        validateCircuitExists(lowerCaseName, circuit);

        if (!circuit.getCreatorId().equals(invokingMemberId)) {
            validateCircuitMembership(circuit, invokingMemberId);
        }

        TerminalCircuitCommandHistoryEntity commandHistory = new TerminalCircuitCommandHistoryEntity();

        commandHistory.setId(UUID.randomUUID());
        commandHistory.setCircuitId(circuit.getId());
        commandHistory.setCommand(commandInputDto.getCommand());
        commandHistory.setArgs(StringUtils.join(commandInputDto.getArgs(), " "));
        commandHistory.setCommandDate(now);

        terminalCircuitCommandHistoryRepository.save(commandHistory);

        return terminalRouteRegistry.routeCommand(organizationId, invokingMemberId, commandInputDto);
    }



    public SimpleStatusDto joinCircuit(UUID organizationId, UUID invokingMemberId, String circuitName) {

        LocalDateTime now = gridClock.now();

        validateNotNull("circuitName", circuitName);

        String lowerCaseCircuitName = circuitName.toLowerCase();

        TerminalCircuitEntity circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, lowerCaseCircuitName);

        validateCircuitExists(lowerCaseCircuitName, circuit);

        CircuitMemberEntity membership = circuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(organizationId, circuit.getId(), invokingMemberId);

        SimpleStatusDto status;

        if (membership == null) {
            membership = new CircuitMemberEntity();
            membership.setId(UUID.randomUUID());
            membership.setOrganizationId(organizationId);
            membership.setMemberId(invokingMemberId);
            membership.setCircuitId(circuit.getId());
            membership.setJoinTime(now);
            membership.setActiveInSession(true);

            circuitMemberRepository.save(membership);

            status = new SimpleStatusDto(Status.JOINED, "Member joined.");
        } else {
            status = new SimpleStatusDto(Status.NO_ACTION, "Member already joined.");
        }

        return status;
    }

    public TerminalCircuitDto getCircuit(UUID organizationId, UUID invokingMemberId, String sessionName) {
        return null;
    }

    public SimpleStatusDto leaveCircuit(UUID organizationId, UUID invokingMemberId, String circuitName) {

        validateNotNull("circuitName", circuitName);

        String lowerCaseCircuitName = circuitName.toLowerCase();

        TerminalCircuitEntity circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, lowerCaseCircuitName);

        validateCircuitExists(lowerCaseCircuitName, circuit);

        CircuitMemberEntity membership = circuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(organizationId, circuit.getId(), invokingMemberId);

        SimpleStatusDto status;

        if (membership != null) {
            membership.setActiveInSession(false);

            circuitMemberRepository.save(membership);

            status = new SimpleStatusDto(Status.SUCCESS, "Member left.");
        } else {
            status = new SimpleStatusDto(Status.NO_ACTION, "Member not in circuit.");
        }

        return status;
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

    private void validateCircuitMembership(TerminalCircuitEntity circuit, UUID invokingMemberId) {
        CircuitMemberEntity membership = circuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(circuit.getOrganizationId(), circuit.getId(), invokingMemberId);

        if (membership == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_JOINED_TO_CIRCUIT, "Member is not joined to terminal circuit "+circuit.getCircuitName());
        }
    }

}

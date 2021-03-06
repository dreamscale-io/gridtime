package com.dreamscale.gridtime.core.capability.circuit;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.journal.JournalEntryDto;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.active.MemberStatusManager;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.security.RequestContext;
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
public class TeamCircuitOperator {

    public static final String ROOM_URN_PREFIX = "/talk/to/room/";

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    private TeamCircuitRoomRepository teamCircuitRoomRepository;

    @Autowired
    private TeamCircuitRepository teamCircuitRepository;

    @Autowired
    private TeamCircuitTalkRoomRepository teamCircuitTalkRoomRepository;

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private MemberStatusManager memberStatusManager;

    @Autowired
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private OrganizationCapability organizationMembership;

    @Autowired
    private TalkRoomMessageRepository talkRoomMessageRepository;

    @Autowired
    private LearningCircuitEventRepository learningCircuitEventRepository;

    private static final String TEAM_ROOM_PREFIX = "team-";
    private static final String TEAM_ROOM_DEFAULT_NAME = "home";


    public UUID getMyTeamCircuitRoomId(LocalDateTime now, UUID organizationId, UUID memberId) {

        UUID teamCircuitRoomId = null;

        TeamDto teamDto = teamCapability.getMyActiveTeam(organizationId, memberId);

        if (teamDto != null) {
            TeamCircuitEntity circuit = teamCircuitRepository.findByTeamId(teamDto.getId());

            if (circuit == null) {
                circuit = createTeamCircuit(now, teamDto, memberId);
            }

            teamCircuitRoomId = circuit.getTeamRoomId();
        }

       return teamCircuitRoomId;
    }



    public void validateMemberIsOwnerOrModeratorOfTeam(UUID organizationId, UUID teamId, UUID invokingMemberId) {

        TeamCircuitEntity circuit = teamCircuitRepository.findByOrganizationIdAndTeamId(organizationId, teamId);

        validateCircuitExists("[team network]", circuit);

        if ((!circuit.getOwnerId().equals(invokingMemberId) && !circuit.getModeratorId().equals(invokingMemberId))) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_MODERATOR_OF_TEAM,
                    "Member must be the owner or moderator of this team");
        }
    }

    public void addMemberToTeamCircuit(LocalDateTime now, UUID organizationId, UUID teamId, UUID memberId) {

        TeamCircuitEntity circuit = teamCircuitRepository.findByOrganizationIdAndTeamId(organizationId, teamId);

        validateCircuitExists("[team network]", circuit);

        TalkRoomMemberEntity talkRoomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, circuit.getTeamRoomId(), memberId);

        if (talkRoomMember == null) {

            talkRoomMember = new TalkRoomMemberEntity();
            talkRoomMember.setId(UUID.randomUUID());
            talkRoomMember.setOrganizationId(organizationId);
            talkRoomMember.setRoomId(circuit.getTeamRoomId());
            talkRoomMember.setMemberId(memberId);
            talkRoomMember.setJoinTime(now);

            talkRoomMemberRepository.save(talkRoomMember);
        }
    }

    public void removeMemberFromTeamCircuit(UUID organizationId, UUID teamId, UUID memberId) {
        TeamCircuitEntity circuit = teamCircuitRepository.findByOrganizationIdAndTeamId(organizationId, teamId);

        validateCircuitExists("[team network]", circuit);

        TalkRoomMemberEntity talkRoomMember = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, circuit.getTeamRoomId(), memberId);

        if (talkRoomMember != null) {

            talkRoomMemberRepository.delete(talkRoomMember);
        }
    }

    public TeamCircuitDto getMyActiveTeamCircuit(UUID organizationId, UUID memberId) {

        TeamDto teamDto = teamCapability.getMyActiveTeam(organizationId, memberId);


        return loadTeamCircuitDto(organizationId, teamDto);
    }

    private TeamCircuitDto loadTeamCircuitDto(UUID organizationId, TeamDto teamDto) {

        TeamCircuitEntity teamCircuitEntity = findOrCreateTeamCircuit(teamDto);

        TeamCircuitDto teamCircuitDto = new TeamCircuitDto();

        teamCircuitDto.setTeamId(teamDto.getId());
        teamCircuitDto.setOrganizationId(organizationId);
        teamCircuitDto.setTeamName(teamDto.getName());
        teamCircuitDto.setTeamMembers(teamDto.getTeamMembers());
        teamCircuitDto.setIsHomeTeam(teamDto.isHomeTeam());

        teamCircuitDto.setOwnerId(teamCircuitEntity.getOwnerId());
        teamCircuitDto.setModeratedId(teamCircuitEntity.getModeratorId());

        String ownerName = memberDetailsRetriever.lookupMemberName(teamCircuitEntity.getOrganizationId(), teamCircuitEntity.getOwnerId());
        String moderatorName = memberDetailsRetriever.lookupMemberName(teamCircuitEntity.getOrganizationId(), teamCircuitEntity.getModeratorId());

        teamCircuitDto.setOwnerName(ownerName);
        teamCircuitDto.setModeratorName(moderatorName);

        TeamCircuitRoomDto defaultRoom = new TeamCircuitRoomDto();
        defaultRoom.setTalkRoomId(teamCircuitEntity.getTeamRoomId());
        defaultRoom.setCircuitRoomName(TEAM_ROOM_DEFAULT_NAME);
        defaultRoom.setTalkRoomName(deriveDefaultTeamRoom(teamDto.getName()));
        defaultRoom.setOwnerId(teamCircuitEntity.getOwnerId());
        defaultRoom.setModeratorId(teamCircuitEntity.getModeratorId());
        defaultRoom.setOwnerName(ownerName);
        defaultRoom.setModeratorName(moderatorName);
        defaultRoom.setCircuitState(TeamCircuitState.ACTIVE.name());

        teamCircuitDto.setDefaultRoom(defaultRoom);

        teamCircuitDto.setTeamRooms(lookupTeamRooms(teamCircuitEntity.getOrganizationId(), teamCircuitEntity.getTeamId()));
        return teamCircuitDto;
    }

    public List<TeamCircuitDto> gettAllMyTeamCircuits(UUID organizationId, UUID memberId) {

        List<TeamDto> teams = teamCapability.getAllMyParticipatingTeamsWithMembers(organizationId, memberId);

        List<TeamCircuitDto> teamCircuitDtos = new ArrayList<>();

        for (TeamDto team : teams) {
            teamCircuitDtos.add(loadTeamCircuitDto(organizationId, team));
        }

        return teamCircuitDtos;
    }

    public void notifyTeamOfIntention(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, JournalEntryDto journalEntryDto) {

        String username = organizationMembership.getUsernameForMemberId(memberFromId);
        TeamCircuitDto teamCircuit = getMyActiveTeamCircuit(organizationId, memberFromId);

        IntentionStartedDetailsDto intentionStartedDetails = new IntentionStartedDetailsDto(username, memberFromId, journalEntryDto);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(memberFromId);
        messageEntity.setToRoomId(teamCircuit.getDefaultRoom().getTalkRoomId());
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.TEAM_INTENTION_STARTED);
        messageEntity.setJsonBody(JSONTransformer.toJson(intentionStartedDetails));

        talkRoomMessageRepository.save(messageEntity);

        String urn = ROOM_URN_PREFIX + teamCircuit.getDefaultRoom().getTalkRoomName();
        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(teamCircuit.getDefaultRoom().getTalkRoomId(), talkMessageDto);
    }

    public void notifyTeamOfIntentionUpdate(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, JournalEntryDto journalEntryDto) {

        String username = organizationMembership.getUsernameForMemberId(memberFromId);
        TeamCircuitDto teamCircuit = getMyActiveTeamCircuit(organizationId, memberFromId);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(memberFromId);
        messageEntity.setToRoomId(teamCircuit.getDefaultRoom().getTalkRoomId());
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.TEAM_INTENTION_UPDATE);
        messageEntity.setJsonBody(JSONTransformer.toJson(journalEntryDto));

        talkRoomMessageRepository.save(messageEntity);

        String urn = ROOM_URN_PREFIX + teamCircuit.getDefaultRoom().getTalkRoomName();
        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(teamCircuit.getDefaultRoom().getTalkRoomId(), talkMessageDto);
    }

    public void notifyTeamOfIntentionAborted(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, JournalEntryDto journalEntryDto) {

        TeamCircuitDto teamCircuit = getMyActiveTeamCircuit(organizationId, memberFromId);

        String username = organizationMembership.getUsernameForMemberId(memberFromId);
        IntentionAbortedDetailsDto intentionFinishedDto = new IntentionAbortedDetailsDto(username, memberFromId, journalEntryDto);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(memberFromId);
        messageEntity.setToRoomId(teamCircuit.getDefaultRoom().getTalkRoomId());
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.TEAM_INTENTION_ABORTED);
        messageEntity.setJsonBody(JSONTransformer.toJson(intentionFinishedDto));

        talkRoomMessageRepository.save(messageEntity);

        String urn = ROOM_URN_PREFIX + teamCircuit.getDefaultRoom().getTalkRoomName();
        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(teamCircuit.getDefaultRoom().getTalkRoomId(), talkMessageDto);
    }

    public void notifyTeamOfIntentionFinished(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, JournalEntryDto journalEntryDto) {

        String username = organizationMembership.getUsernameForMemberId(memberFromId);
        TeamCircuitDto teamCircuit = getMyActiveTeamCircuit(organizationId, memberFromId);

        IntentionFinishedDetailsDto intentionFinishedDto = new IntentionFinishedDetailsDto(username, memberFromId, journalEntryDto);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(memberFromId);
        messageEntity.setToRoomId(teamCircuit.getDefaultRoom().getTalkRoomId());
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.TEAM_INTENTION_FINISHED);
        messageEntity.setJsonBody(JSONTransformer.toJson(intentionFinishedDto));

        talkRoomMessageRepository.save(messageEntity);

        String urn = ROOM_URN_PREFIX + teamCircuit.getDefaultRoom().getTalkRoomName();
        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(teamCircuit.getDefaultRoom().getTalkRoomId(), talkMessageDto);
    }

    public void notifyTeamOfWTFStarted(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {


        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_STARTED);
    }

    public void notifyTeamOfWTFOnHold(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_ON_HOLD);
    }

    public void notifyTeamOfWTFResumed(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_RESUMED);
    }

    public void notifyTeamOfWTFSolved(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_SOLVED);
    }

    public void notifyTeamOfWTFCanceled(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_CANCELED);
    }

    public void notifyTeamOfWTFClosed(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_CLOSED);
    }

    public void notifyTeamOfWTFJoined(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_JOINED);
    }

    public void notifyTeamOfWTFLeft(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_WTF_LEAVE);
    }

    public void notifyTeamOfRetroStarted(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_RETRO_STARTED);
    }

    public void notifyTeamOfRetroClosed(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto) {

        createCircuitEventAndNotifyTeam(organizationId, memberFromId, now, nanoTime, circuitDto, CircuitMessageType.TEAM_RETRO_CLOSED);
    }

    private void createCircuitEventAndNotifyTeam(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, LearningCircuitDto circuitDto, CircuitMessageType messageType) {
        String username = organizationMembership.getUsernameForMemberId(memberFromId);

        UUID teamRoomId = getMyTeamCircuitRoomId(now, organizationId, memberFromId);
        TalkRoomEntity teamRoom = talkRoomRepository.findById(teamRoomId);


        LearningCircuitEventEntity circuitEventEntity = new LearningCircuitEventEntity();
        circuitEventEntity.setId(UUID.randomUUID());
        circuitEventEntity.setOrganizationId(organizationId);
        circuitEventEntity.setCircuitId(circuitDto.getId());
        circuitEventEntity.setCircuitMessageType(messageType);
        circuitEventEntity.setFromMemberId(memberFromId);
        circuitEventEntity.setPosition(now);
        circuitEventEntity.setNanoTime(nanoTime);

        learningCircuitEventRepository.save(circuitEventEntity);

        WTFStatusUpdateDto wtfStatusUpdateDto = new WTFStatusUpdateDto(username, memberFromId, messageType.name(), messageType.getStatusMessage(), circuitDto);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(memberFromId);
        messageEntity.setToRoomId(teamRoomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(messageType);
        messageEntity.setJsonBody(JSONTransformer.toJson(wtfStatusUpdateDto));

        talkRoomMessageRepository.save(messageEntity);

        String urn = ROOM_URN_PREFIX + teamRoom.getRoomName();

        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(teamRoomId, talkMessageDto);
    }


    public void notifyTeamOfMemberStatusUpdate(UUID organizationId, UUID memberFromId, LocalDateTime now, Long nanoTime, TeamMemberDto memberStatusDto) {

        UUID teamRoomId = getMyTeamCircuitRoomId(now, organizationId, memberFromId);

        if (teamRoomId != null) {
            TalkRoomEntity teamRoom = talkRoomRepository.findById(teamRoomId);

            TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
            messageEntity.setId(UUID.randomUUID());
            messageEntity.setFromId(memberFromId);
            messageEntity.setToRoomId(teamRoomId);
            messageEntity.setPosition(now);
            messageEntity.setNanoTime(nanoTime);
            messageEntity.setMessageType(CircuitMessageType.TEAM_MEMBER_STATUS_UPDATE);
            messageEntity.setJsonBody(JSONTransformer.toJson(memberStatusDto));

            String urn = ROOM_URN_PREFIX + teamRoom.getRoomName();

            TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

            talkRouter.sendRoomMessage(teamRoomId, talkMessageDto);

            talkRoomMessageRepository.save(messageEntity);
        }
    }

    public void notifyTeamOfXPUpdate(UUID organizationId, UUID fromMemberId, UUID forMemberId, LocalDateTime now, Long nanoTime, XPSummaryDto oldXPSummary, XPSummaryDto newXPSummary) {

        String username = organizationMembership.getUsernameForMemberId(forMemberId);
        XPStatusUpdateDto xpStatusUpdateDto = new XPStatusUpdateDto(username, forMemberId, oldXPSummary, newXPSummary);

        UUID teamRoomId = getMyTeamCircuitRoomId(now, organizationId, fromMemberId);

        TalkRoomEntity teamRoom = talkRoomRepository.findById(teamRoomId);

        TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
        messageEntity.setId(UUID.randomUUID());
        messageEntity.setFromId(fromMemberId);
        messageEntity.setToRoomId(teamRoomId);
        messageEntity.setPosition(now);
        messageEntity.setNanoTime(nanoTime);
        messageEntity.setMessageType(CircuitMessageType.TEAM_MEMBER_XP_UPDATE);
        messageEntity.setJsonBody(JSONTransformer.toJson(xpStatusUpdateDto));

        String urn = ROOM_URN_PREFIX + teamRoom.getRoomName();

        TalkMessageDto talkMessageDto = toTalkMessageDto(urn, messageEntity);

        talkRouter.sendRoomMessage(teamRoomId, talkMessageDto);

        talkRoomMessageRepository.save(messageEntity);
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

    public TeamCircuitRoomDto createTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {

        TeamCircuitEntity circuitEntity = teamCircuitRepository.findByOrganizationIdAndTeamName(organizationId, teamName);

        validateCircuitExists(teamName, circuitEntity);

        TeamCircuitRoomEntity teamRoom = createTeamRoom(circuitEntity, teamName, roomName);

        TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();

        teamCircuitRoomDto.setCircuitRoomName(teamRoom.getLocalName());
        teamCircuitRoomDto.setTalkRoomId(teamRoom.getTalkRoomId());
        teamCircuitRoomDto.setTalkRoomName(deriveTeamRoomNameForTalk(teamName, roomName));
        teamCircuitRoomDto.setOwnerId(circuitEntity.getOwnerId());
        teamCircuitRoomDto.setModeratorId(circuitEntity.getModeratorId());

        String ownerName = memberDetailsRetriever.lookupMemberName(circuitEntity.getOrganizationId(), circuitEntity.getOwnerId());
        String moderatorName = memberDetailsRetriever.lookupMemberName(circuitEntity.getOrganizationId(), circuitEntity.getModeratorId());

        teamCircuitRoomDto.setOwnerName(ownerName);
        teamCircuitRoomDto.setModeratorName(moderatorName);
        teamCircuitRoomDto.setCircuitState(teamRoom.getCircuitState().name());

        return teamCircuitRoomDto;
    }

    private void validateCircuitExists(String circuitName, TeamCircuitEntity teamCircuitEntity) {
        if (teamCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find team network: " + circuitName);
        }
    }

    private void validateTeamRoomDoesntAlreadyExist(TeamCircuitRoomEntity teamRoom) {
        if (teamRoom != null) {
            throw new BadRequestException(ValidationErrorCodes.CIRCUIT_ALREADY_EXISTS, "Team Circuit already exists: " + teamRoom.getLocalName());
        }
    }


    private void validateRoomExists(String roomName, TeamCircuitTalkRoomEntity teamRoom) {
        if (teamRoom == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ROOM, "Unable to find team room: " + roomName);
        }
    }

    private void validateRoomExists(String roomName, TeamCircuitRoomEntity teamRoom) {
        if (teamRoom == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ROOM, "Unable to find team room: " + roomName);
        }
    }

    public TeamCircuitRoomDto getTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {

        TeamCircuitTalkRoomEntity teamRoom = teamCircuitTalkRoomRepository.findByOrganizationIdAndTeamNameAndCircuitRoomName(organizationId, teamName, roomName);

        validateRoomExists(roomName, teamRoom);

        TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();

        teamCircuitRoomDto.setCircuitRoomName(teamRoom.getCircuitRoomName());
        teamCircuitRoomDto.setTalkRoomId(teamRoom.getTalkRoomId());
        teamCircuitRoomDto.setTalkRoomName(teamRoom.getTalkRoomName());
        teamCircuitRoomDto.setOwnerId(teamRoom.getOwnerId());
        teamCircuitRoomDto.setModeratorId(teamRoom.getModeratorId());

        String ownerName = memberDetailsRetriever.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getOwnerId());
        String moderatorName = memberDetailsRetriever.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getModeratorId());

        teamCircuitRoomDto.setOwnerName(ownerName);
        teamCircuitRoomDto.setModeratorName(moderatorName);

        teamCircuitRoomDto.setDescription(teamRoom.getDescription());
        teamCircuitRoomDto.setJsonTags(teamRoom.getJsonTags());
        teamCircuitRoomDto.setCircuitState(teamRoom.getCircuitState().name());

        return teamCircuitRoomDto;
    }


    public TeamCircuitRoomDto closeTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {

        TeamCircuitRoomEntity teamRoom = teamCircuitRoomRepository.findByOrganizationIdTeamNameAndLocalName(organizationId, teamName, roomName);

        validateRoomExists(roomName, teamRoom);

        teamRoom.setCloseTime(gridClock.now());
        teamRoom.setCircuitState(TeamCircuitState.CLOSED);

        teamCircuitRoomRepository.save(teamRoom);

        return createTeamCircuitRoomDto(teamName, roomName, teamRoom);
    }

    private TeamCircuitRoomDto createTeamCircuitRoomDto(String teamName, String roomName, TeamCircuitRoomEntity teamRoom) {
        TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();

        teamCircuitRoomDto.setCircuitRoomName(roomName);
        teamCircuitRoomDto.setTalkRoomId(teamRoom.getTalkRoomId());
        teamCircuitRoomDto.setTalkRoomName(deriveTeamRoomNameForTalk(teamName, roomName));
        teamCircuitRoomDto.setOwnerId(teamRoom.getOwnerId());
        teamCircuitRoomDto.setModeratorId(teamRoom.getModeratorId());

        String ownerName = memberDetailsRetriever.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getOwnerId());
        String moderatorName = memberDetailsRetriever.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getModeratorId());

        teamCircuitRoomDto.setOwnerName(ownerName);
        teamCircuitRoomDto.setModeratorName(moderatorName);
        teamCircuitRoomDto.setDescription(teamRoom.getDescription());
        teamCircuitRoomDto.setJsonTags(teamRoom.getJsonTags());
        teamCircuitRoomDto.setCircuitState(teamRoom.getCircuitState().name());
        return teamCircuitRoomDto;
    }

    public TeamCircuitDto getTeamCircuitByOrganizationAndName(UUID organizationId, String teamName) {
        //TODO retrieve team circuit for a team other than yours...
        return null;
    }


    public TeamCircuitRoomDto saveDescriptionForTeamCircuitRoom(UUID organizationId, String teamName, String roomName, DescriptionInputDto descriptionInputDto) {

        TeamCircuitRoomEntity teamRoom = teamCircuitRoomRepository.findByOrganizationIdTeamNameAndLocalName(organizationId, teamName, roomName);

        validateRoomExists(roomName, teamRoom);

        teamRoom.setDescription(descriptionInputDto.getDescription());
        teamCircuitRoomRepository.save(teamRoom);

        return createTeamCircuitRoomDto(teamName, roomName, teamRoom);
    }

    public TeamCircuitRoomDto saveTagsForTeamCircuitRoom(UUID organizationId, String teamName, String roomName, TagsInputDto tagsInputDto) {
        TeamCircuitRoomEntity teamRoom = teamCircuitRoomRepository.findByOrganizationIdTeamNameAndLocalName(organizationId, teamName, roomName);

        validateRoomExists(roomName, teamRoom);

        teamRoom.setJsonTags(JSONTransformer.toJson(tagsInputDto));
        teamCircuitRoomRepository.save(teamRoom);

        return createTeamCircuitRoomDto(teamName, roomName, teamRoom);

    }

    private List<TeamCircuitRoomDto> lookupTeamRooms(UUID organizationId, UUID teamId) {

        List<TeamCircuitTalkRoomEntity> teamRooms = teamCircuitTalkRoomRepository.findByOrganizationIdAndTeamId(organizationId, teamId);

        List<TeamCircuitRoomDto> teamCircuitRoomDtos = new ArrayList<>();

        for (TeamCircuitTalkRoomEntity room: teamRooms) {
            TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();

            teamCircuitRoomDto.setCircuitRoomName(room.getCircuitRoomName());
            teamCircuitRoomDto.setTalkRoomId(room.getTalkRoomId());
            teamCircuitRoomDto.setTalkRoomName(room.getTalkRoomName());
            teamCircuitRoomDto.setOwnerId(room.getOwnerId());
            teamCircuitRoomDto.setModeratorId(room.getModeratorId());

            teamCircuitRoomDto.setOwnerName(room.getOwnerName());
            teamCircuitRoomDto.setModeratorName(room.getModeratorName());
            teamCircuitRoomDto.setDescription(room.getDescription());
            teamCircuitRoomDto.setJsonTags(room.getJsonTags());
            teamCircuitRoomDto.setCircuitState(room.getCircuitState().name());

            teamCircuitRoomDtos.add(teamCircuitRoomDto);
        }

        return teamCircuitRoomDtos;
    }

    private TeamCircuitEntity findOrCreateTeamCircuit(TeamDto team) {

        TeamCircuitEntity teamCircuit = teamCircuitRepository.findByTeamId(team.getId());

        if (teamCircuit == null) {
            teamCircuit = createTeamCircuit(team);
        }

        return teamCircuit;
    }

    @Transactional
    public TeamCircuitRoomEntity createTeamRoom(TeamCircuitEntity teamCircuit, String teamName, String roomName) {

        log.info("Team ID : "+teamCircuit.getTeamId());

        TeamCircuitRoomEntity existingRoom = teamCircuitRoomRepository.findByTeamIdAndLocalName(teamCircuit.getTeamId(), roomName);
        validateTeamRoomDoesntAlreadyExist(existingRoom);

        TalkRoomEntity talkRoomEntity = new TalkRoomEntity();
        talkRoomEntity.setId(UUID.randomUUID());
        talkRoomEntity.setOrganizationId(teamCircuit.getOrganizationId());
        talkRoomEntity.setRoomType(RoomType.TEAM_ROOM);
        talkRoomEntity.setRoomName(deriveTeamRoomNameForTalk(teamName, roomName));

        talkRoomRepository.save(talkRoomEntity);

        LocalDateTime now = gridClock.now();

        TeamCircuitRoomEntity teamRoom = new TeamCircuitRoomEntity();
        teamRoom.setId(UUID.randomUUID());
        teamRoom.setLocalName(roomName);
        teamRoom.setOrganizationId(teamCircuit.getOrganizationId());
        teamRoom.setTeamId(teamCircuit.getTeamId());
        teamRoom.setTalkRoomId(talkRoomEntity.getId());
        teamRoom.setOwnerId(teamCircuit.getOwnerId());
        teamRoom.setModeratorId(teamCircuit.getModeratorId());
        teamRoom.setCircuitState(TeamCircuitState.ACTIVE);
        teamRoom.setOpenTime(now);

        teamCircuitRoomRepository.save(teamRoom);

        TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
        talkRoomMember.setId(UUID.randomUUID());
        talkRoomMember.setOrganizationId(teamCircuit.getOrganizationId());
        talkRoomMember.setMemberId(teamCircuit.getOwnerId());
        talkRoomMember.setRoomId(talkRoomEntity.getId());
        talkRoomMember.setJoinTime(now);

        talkRoomMemberRepository.save(talkRoomMember);

        return teamRoom;
    }

    private String deriveTeamRoomNameForTalk(String teamName, String roomName) {
        return TEAM_ROOM_PREFIX + teamName + "-" + roomName;
    }


    public TeamCircuitEntity createTeamCircuit(LocalDateTime now, TeamDto team, UUID ownerId) {
        TalkRoomEntity defaultTalkRoom = new TalkRoomEntity();
        defaultTalkRoom.setId(UUID.randomUUID());
        defaultTalkRoom.setOrganizationId(team.getOrganizationId());
        defaultTalkRoom.setRoomType(RoomType.TEAM_ROOM);
        defaultTalkRoom.setRoomName(deriveDefaultTeamRoom(team.getName()));

        talkRoomRepository.save(defaultTalkRoom);

        TeamCircuitEntity teamCircuitEntity = new TeamCircuitEntity();
        teamCircuitEntity.setId(UUID.randomUUID());
        teamCircuitEntity.setOrganizationId(team.getOrganizationId());
        teamCircuitEntity.setTeamId(team.getId());
        teamCircuitEntity.setTeamRoomId(defaultTalkRoom.getId());
        teamCircuitEntity.setOwnerId(ownerId);
        teamCircuitEntity.setModeratorId(ownerId);

        teamCircuitRepository.save(teamCircuitEntity);

        TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
        talkRoomMember.setId(UUID.randomUUID());
        talkRoomMember.setOrganizationId(team.getOrganizationId());
        talkRoomMember.setMemberId(ownerId);
        talkRoomMember.setRoomId(defaultTalkRoom.getId());
        talkRoomMember.setJoinTime(now);

        talkRoomMemberRepository.save(talkRoomMember);

        return teamCircuitEntity;
    }

    private TeamCircuitEntity createTeamCircuit(TeamDto team) {
        TalkRoomEntity defaultTalkRoom = new TalkRoomEntity();
        defaultTalkRoom.setId(UUID.randomUUID());
        defaultTalkRoom.setOrganizationId(team.getOrganizationId());
        defaultTalkRoom.setRoomType(RoomType.TEAM_ROOM);
        defaultTalkRoom.setRoomName(deriveDefaultTeamRoom(team.getName()));

        talkRoomRepository.save(defaultTalkRoom);

        TeamCircuitEntity teamCircuitEntity = new TeamCircuitEntity();
        teamCircuitEntity.setId(UUID.randomUUID());
        teamCircuitEntity.setOrganizationId(team.getOrganizationId());
        teamCircuitEntity.setTeamId(team.getId());
        teamCircuitEntity.setTeamRoomId(defaultTalkRoom.getId());
        teamCircuitEntity.setOwnerId(getFirstMemberId(team.getTeamMembers()));
        teamCircuitEntity.setModeratorId(getFirstMemberId(team.getTeamMembers()));

        teamCircuitRepository.save(teamCircuitEntity);

        LocalDateTime now = gridClock.now();

        List<TalkRoomMemberEntity> talkRoomMembers = new ArrayList<>();

        for (TeamMemberDto teamMember : team.getTeamMembers()) {

            TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
            talkRoomMember.setId(UUID.randomUUID());
            talkRoomMember.setOrganizationId(team.getOrganizationId());
            talkRoomMember.setMemberId(teamMember.getId());
            talkRoomMember.setRoomId(defaultTalkRoom.getId());
            talkRoomMember.setJoinTime(now);

            talkRoomMembers.add(talkRoomMember);
        }

        talkRoomMemberRepository.save(talkRoomMembers);

        return teamCircuitEntity;
    }

    private UUID getFirstMemberId(List<TeamMemberDto> teamMembers) {
        UUID memberId = null;

        if (teamMembers != null && teamMembers.size() > 0) {
            memberId = teamMembers.get(0).getId();
        }

        return memberId;
    }

    private String deriveDefaultTeamRoom(String teamName) {
        return TEAM_ROOM_PREFIX + teamName + "-"+TEAM_ROOM_DEFAULT_NAME;
    }



}

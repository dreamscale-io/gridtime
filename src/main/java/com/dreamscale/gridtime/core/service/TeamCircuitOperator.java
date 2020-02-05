package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.organization.MemberWorkStatusDto;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
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
    private TeamService teamService;

    @Autowired
    private MemberStatusService memberStatusService;

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    private TimeService timeService;

    private static final String TEAM_ROOM_PREFIX = "team-";
    private static final String TEAM_ROOM_DEFAULT_NAME = "home";

    public TeamCircuitDto getMyPrimaryTeamCircuit(UUID organizationId, UUID memberId) {

        TeamDto teamDto = teamService.getMyPrimaryTeam(organizationId, memberId);

        List<MemberWorkStatusDto> members = memberStatusService.getStatusOfMeAndMyTeam(organizationId, memberId);

       TeamCircuitEntity teamCircuitEntity = findOrCreateTeamCircuit(teamDto, members);

        TeamCircuitDto teamCircuitDto = new TeamCircuitDto();

        teamCircuitDto.setTeamId(teamDto.getId());
        teamCircuitDto.setOrganizationId(organizationId);
        teamCircuitDto.setTeamName(teamDto.getName());
        teamCircuitDto.setTeamMembers(members);

        teamCircuitDto.setDefaultRoom(createDefaultRoom(teamCircuitEntity.getTeamRoomId(), teamDto.getName()));
        teamCircuitDto.setOwnerId(teamCircuitEntity.getOwnerId());
        teamCircuitDto.setOwnerName(memberDetailsService.lookupMemberName(teamCircuitEntity.getOrganizationId(), teamCircuitEntity.getOwnerId()));

        teamCircuitDto.setModeratedId(teamCircuitEntity.getModeratorId());
        teamCircuitDto.setModeratorName(memberDetailsService.lookupMemberName(teamCircuitEntity.getOrganizationId(), teamCircuitEntity.getModeratorId()));

        teamCircuitDto.setTeamRooms(lookupTeamRooms(teamCircuitEntity.getOrganizationId(), teamCircuitEntity.getTeamId()));

        return teamCircuitDto;
    }

    private TeamCircuitRoomDto createDefaultRoom(UUID roomId, String teamName) {

        TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();
        teamCircuitRoomDto.setTalkRoomId(roomId);
        teamCircuitRoomDto.setCircuitRoomName(TEAM_ROOM_DEFAULT_NAME);
        teamCircuitRoomDto.setTalkRoomName(deriveDefaultTeamRoom(teamName));

        return teamCircuitRoomDto;
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

        String memberName = memberDetailsService.lookupMemberName(circuitEntity.getOrganizationId(), circuitEntity.getOwnerId());

        teamCircuitRoomDto.setOwnerName(memberName);
        teamCircuitRoomDto.setModeratorName(memberName);

        return teamCircuitRoomDto;
    }

    private void validateCircuitExists(String circuitName, TeamCircuitEntity teamCircuitEntity) {
        if (teamCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find team circuit: " + circuitName);
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

        String ownerName = memberDetailsService.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getOwnerId());
        String moderatorName = memberDetailsService.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getModeratorId());

        teamCircuitRoomDto.setOwnerName(ownerName);
        teamCircuitRoomDto.setModeratorName(moderatorName);

        teamCircuitRoomDto.setDescription(teamRoom.getDescription());
        teamCircuitRoomDto.setJsonTags(teamRoom.getJsonTags());

        return teamCircuitRoomDto;
    }


    public TeamCircuitRoomDto closeTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {

        TeamCircuitRoomEntity teamRoom = teamCircuitRoomRepository.findByOrganizationIdTeamNameAndLocalName(organizationId, teamName, roomName);

        validateRoomExists(roomName, teamRoom);

        teamRoom.setCloseTime(timeService.now());
        teamRoom.setCircuitStatus(CircuitStatus.CLOSED);

        teamCircuitRoomRepository.save(teamRoom);

        TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();

        teamCircuitRoomDto.setCircuitRoomName(roomName);
        teamCircuitRoomDto.setTalkRoomId(teamRoom.getTalkRoomId());
        teamCircuitRoomDto.setTalkRoomName(deriveTeamRoomNameForTalk(teamName, roomName));
        teamCircuitRoomDto.setOwnerId(teamRoom.getOwnerId());
        teamCircuitRoomDto.setModeratorId(teamRoom.getModeratorId());

        String ownerName = memberDetailsService.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getOwnerId());
        String moderatorName = memberDetailsService.lookupMemberName(teamRoom.getOrganizationId(), teamRoom.getModeratorId());

        teamCircuitRoomDto.setOwnerName(ownerName);
        teamCircuitRoomDto.setModeratorName(moderatorName);
        teamCircuitRoomDto.setDescription(teamRoom.getDescription());
        teamCircuitRoomDto.setJsonTags(teamRoom.getJsonTags());

        return teamCircuitRoomDto;
    }

    public TeamCircuitDto getTeamCircuitByOrganizationAndName(UUID organizationId, String teamName) {
        //TODO retrieve team circuit for a team other than yours...
        return null;
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

            teamCircuitRoomDtos.add(teamCircuitRoomDto);
        }

        return teamCircuitRoomDtos;
    }

    private TeamCircuitEntity findOrCreateTeamCircuit(TeamDto team, List<MemberWorkStatusDto> teamMembers) {

        TeamCircuitEntity teamCircuit = teamCircuitRepository.findByTeamId(team.getId());

        if (teamCircuit == null) {
            teamCircuit = createTeamCircuit(team, teamMembers);
        }

        return teamCircuit;
    }

    @Transactional
    private TeamCircuitRoomEntity createTeamRoom(TeamCircuitEntity teamCircuit, String teamName, String roomName) {

        TeamCircuitRoomEntity existingRoom = teamCircuitRoomRepository.findByTeamIdAndLocalName(teamCircuit.getTeamId(), roomName);
        validateTeamRoomDoesntAlreadyExist(existingRoom);

        TalkRoomEntity talkRoomEntity = new TalkRoomEntity();
        talkRoomEntity.setId(UUID.randomUUID());
        talkRoomEntity.setOrganizationId(teamCircuit.getOrganizationId());
        talkRoomEntity.setRoomType(RoomType.TEAM_ROOM);
        talkRoomEntity.setRoomName(deriveTeamRoomNameForTalk(teamName, roomName));

        talkRoomRepository.save(talkRoomEntity);

        LocalDateTime now = timeService.now();

        TeamCircuitRoomEntity teamRoom = new TeamCircuitRoomEntity();
        teamRoom.setId(UUID.randomUUID());
        teamRoom.setLocalName(roomName);
        teamRoom.setOrganizationId(teamCircuit.getOrganizationId());
        teamRoom.setTeamId(teamCircuit.getTeamId());
        teamRoom.setTalkRoomId(talkRoomEntity.getId());
        teamRoom.setCircuitStatus(CircuitStatus.ACTIVE);
        teamRoom.setOpenTime(now);

        teamCircuitRoomRepository.save(teamRoom);

        TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
        talkRoomMember.setId(UUID.randomUUID());
        talkRoomMember.setOrganizationId(teamCircuit.getOrganizationId());
        talkRoomMember.setMemberId(teamCircuit.getOwnerId());
        talkRoomMember.setRoomId(talkRoomEntity.getId());
        talkRoomMember.setLastActive(now);
        talkRoomMember.setJoinTime(now);
        talkRoomMember.setRoomStatus(RoomMemberStatus.INACTIVE);

        talkRoomMemberRepository.save(talkRoomMember);

        return teamRoom;
    }

    private String deriveTeamRoomNameForTalk(String teamName, String roomName) {
        return TEAM_ROOM_PREFIX + teamName + "-" + roomName;
    }


    @Transactional
    private TeamCircuitEntity createTeamCircuit(TeamDto team, List<MemberWorkStatusDto> teamMembers) {
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
        teamCircuitEntity.setOwnerId(getFirstMemberId(teamMembers));
        teamCircuitEntity.setModeratorId(getFirstMemberId(teamMembers));

        teamCircuitRepository.save(teamCircuitEntity);

        LocalDateTime now = timeService.now();

        List<TalkRoomMemberEntity> talkRoomMembers = new ArrayList<>();

        for (MemberWorkStatusDto teamMember : teamMembers) {

            TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
            talkRoomMember.setId(UUID.randomUUID());
            talkRoomMember.setOrganizationId(team.getOrganizationId());
            talkRoomMember.setMemberId(teamMember.getId());
            talkRoomMember.setRoomId(defaultTalkRoom.getId());
            talkRoomMember.setLastActive(now);
            talkRoomMember.setJoinTime(now);

            if (teamMember.getOnlineStatus() == OnlineStatus.Online) {
                talkRoomMember.setRoomStatus(RoomMemberStatus.ACTIVE);
            }
            else {
                talkRoomMember.setRoomStatus(RoomMemberStatus.INACTIVE);
            }

            talkRoomMembers.add(talkRoomMember);
        }

        talkRoomMemberRepository.save(talkRoomMembers);

        return teamCircuitEntity;
    }

    private UUID getFirstMemberId(List<MemberWorkStatusDto> teamMembers) {
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

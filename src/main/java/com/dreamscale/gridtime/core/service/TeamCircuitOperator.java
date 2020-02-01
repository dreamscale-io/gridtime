package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.organization.MemberWorkStatusDto;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.member.*;
import lombok.extern.slf4j.Slf4j;
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
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamCircuitRepository teamCircuitRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MemberStatusService memberStatusService;

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    private TimeService timeService;

    private static final String TEAM_ROOM_PREFIX = "team-";
    private static final String TEAM_ROOM_DEFAULT_NAME = "default";

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

    private List<TeamCircuitRoomDto> lookupTeamRooms(UUID organizationId, UUID teamId) {
        return new ArrayList<>();
    }



    private TeamCircuitRoomDto createDefaultRoom(UUID roomId, String teamName) {

        TeamCircuitRoomDto teamCircuitRoomDto = new TeamCircuitRoomDto();
        teamCircuitRoomDto.setTalkRoomId(roomId);
        teamCircuitRoomDto.setCircuitRoomName(TEAM_ROOM_DEFAULT_NAME);
        teamCircuitRoomDto.setTalkRoomName(deriveDefaultTeamRoom(teamName));

        return teamCircuitRoomDto;
    }

    public TeamCircuitRoomDto createTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {
        return null;
    }

    public TeamCircuitRoomDto getTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {
        return null;
    }

    public TeamCircuitRoomDto closeTeamCircuitRoom(UUID organizationId, String teamName, String roomName) {
        return null;
    }

    public TeamCircuitDto getTeamCircuitByOrganizationAndName(UUID organizationId, String teamName) {
        return null;
    }


    private TeamCircuitEntity findOrCreateTeamCircuit(TeamDto team, List<MemberWorkStatusDto> teamMembers) {

        TeamCircuitEntity teamCircuit = teamCircuitRepository.findByTeamId(team.getId());

        if (teamCircuit == null) {
            teamCircuit = createTeamCircuit(team, teamMembers);
        }

        return teamCircuit;
    }

    private TeamCircuitEntity createTeamCircuit(TeamDto team, List<MemberWorkStatusDto> teamMembers) {
        TalkRoomEntity talkRoomEntity = new TalkRoomEntity();
        talkRoomEntity.setId(UUID.randomUUID());
        talkRoomEntity.setOrganizationId(team.getOrganizationId());
        talkRoomEntity.setRoomType(RoomType.TEAM_ROOM);
        talkRoomEntity.setOwnerId(teamMembers.get(0).getId());
        talkRoomEntity.setRoomName(deriveDefaultTeamRoom(team.getName()));

        talkRoomRepository.save(talkRoomEntity);

        TeamCircuitEntity teamCircuitEntity = new TeamCircuitEntity();
        teamCircuitEntity.setId(UUID.randomUUID());
        teamCircuitEntity.setOrganizationId(team.getOrganizationId());
        teamCircuitEntity.setTeamId(team.getId());
        teamCircuitEntity.setTeamRoomId(talkRoomEntity.getId());

        teamCircuitRepository.save(teamCircuitEntity);

        LocalDateTime now = timeService.now();

        List<TalkRoomMemberEntity> talkRoomMembers = new ArrayList<>();

        for (MemberWorkStatusDto teamMember : teamMembers) {

            TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
            talkRoomMember.setId(UUID.randomUUID());
            talkRoomMember.setOrganizationId(team.getOrganizationId());
            talkRoomMember.setMemberId(teamMember.getId());
            talkRoomMember.setRoomId(talkRoomEntity.getId());
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

    private String deriveDefaultTeamRoom(String teamName) {
        return TEAM_ROOM_PREFIX + teamName + "-"+TEAM_ROOM_DEFAULT_NAME;
    }


    @Transactional
    private void createTeamCircuit(UUID memberId, TeamEntity team) {

        TalkRoomEntity talkRoomEntity = new TalkRoomEntity();
        talkRoomEntity.setId(UUID.randomUUID());
        talkRoomEntity.setOrganizationId(team.getOrganizationId());
        talkRoomEntity.setRoomType(RoomType.TEAM_ROOM);
        talkRoomEntity.setOwnerId(memberId);

        talkRoomRepository.save(talkRoomEntity);

        TeamCircuitEntity teamCircuitEntity = new TeamCircuitEntity();
        teamCircuitEntity.setId(UUID.randomUUID());
        teamCircuitEntity.setOrganizationId(team.getOrganizationId());
        teamCircuitEntity.setTeamId(team.getId());
        teamCircuitEntity.setTeamRoomId(talkRoomEntity.getId());

        teamCircuitRepository.save(teamCircuitEntity);

        List<MemberStatusEntity> teamMembers = memberStatusService.getTeamMemberStatuses(team.getId());

        LocalDateTime now = timeService.now();

        List<TalkRoomMemberEntity> talkRoomMembers = new ArrayList<>();

        for (MemberStatusEntity teamMember : teamMembers) {

            TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
            talkRoomMember.setId(UUID.randomUUID());
            talkRoomMember.setOrganizationId(team.getOrganizationId());
            talkRoomMember.setMemberId(teamMember.getId());
            talkRoomMember.setRoomId(talkRoomEntity.getId());
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

    }



}

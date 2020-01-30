package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
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
public class TeamCircuitOperator {

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    private TalkRoomMessageRepository talkRoomMessageRepository;

    @Autowired
    private CircuitTalkRoomRepository circuitTalkRoomRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamCircuitRepository teamCircuitRepository;

    @Autowired
    ActiveStatusService activeStatusService;

    @Autowired
    private TimeService timeService;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private MapperFactory mapperFactory;


    public void createTeamLearningCircuitIfDoesntExist(UUID organizationId, UUID memberId) {

        List<TeamEntity> myTeams = teamRepository.findMyTeamsByOrgMembership(organizationId, memberId);

        for (TeamEntity team: myTeams) {

            TeamCircuitEntity teamCircuit = teamCircuitRepository.findByTeamId(team.getId());
            if (teamCircuit == null) {
                createTeamCircuit(memberId, team);
            }
        }
    }

    @Transactional
    private void createTeamCircuit(UUID memberId, TeamEntity team) {

        TalkRoomEntity talkRoomEntity = new TalkRoomEntity();
        talkRoomEntity.setId(UUID.randomUUID());
        talkRoomEntity.setOrganizationId(team.getOrganizationId());
        talkRoomEntity.setRoomType(RoomType.TEAM_STATUS_ROOM);
        talkRoomEntity.setOwnerId(memberId);

        talkRoomRepository.save(talkRoomEntity);

        TeamCircuitEntity teamCircuitEntity = new TeamCircuitEntity();
        teamCircuitEntity.setId(UUID.randomUUID());
        teamCircuitEntity.setOrganizationId(team.getOrganizationId());
        teamCircuitEntity.setTeamId(team.getId());
        teamCircuitEntity.setStatusRoomId(talkRoomEntity.getId());

        teamCircuitRepository.save(teamCircuitEntity);

        List<TeamMemberEntity> teamMembers = teamMemberRepository.findByTeamId(team.getId());

        LocalDateTime now = timeService.now();

        List<TalkRoomMemberEntity> talkRoomMembers = new ArrayList<>();

        for (TeamMemberEntity teamMember : teamMembers) {

            TalkRoomMemberEntity talkRoomMember = new TalkRoomMemberEntity();
            talkRoomMember.setId(UUID.randomUUID());
            talkRoomMember.setOrganizationId(team.getOrganizationId());
            talkRoomMember.setMemberId(teamMember.getMemberId());
            talkRoomMember.setRoomId(talkRoomEntity.getId());
            talkRoomMember.setLastActive(now);
            talkRoomMember.setJoinTime(now);
            talkRoomMember.setRoomStatus(RoomMemberStatus.INACTIVE);

            talkRoomMembers.add(talkRoomMember);
        }

        talkRoomMemberRepository.save(talkRoomMembers);

    }


}

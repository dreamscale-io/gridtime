package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.organization.MemberWorkStatusDto;
import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.gridtime.core.capability.operator.TeamCircuitOperator;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusRepository;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusRepository;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.service.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ActiveWorkStatusManager {

    @Autowired
    ActiveWorkStatusRepository activeWorkStatusRepository;

    @Autowired
    TeamMemberWorkStatusRepository teamMemberWorkStatusRepository;

    @Autowired
    private MemberStatusCapability memberStatusCapability;

    @Autowired
    TeamCircuitOperator teamCircuitOperator;

    @Autowired
    GridClock gridClock;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<TeamMemberWorkStatusDto, TeamMemberWorkStatusEntity> teamMemberStatusMapper;


    @PostConstruct
    private void init() {
        teamMemberStatusMapper = mapperFactory.createDtoEntityMapper(TeamMemberWorkStatusDto.class, TeamMemberWorkStatusEntity.class);
    }

    //rename circuitId to circuitId

    //when team member pushes WTF, notify room
    //when team member resolves WTF, notify room
    //when team member updates their intention, notify room
    //when team member goes online/offline, notify room

    @Transactional
    public void pushWTFStatus(UUID organizationId, UUID memberId, UUID circuitId, LocalDateTime now, Long nanoTime) {

        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity == null) {
            activeWorkStatusEntity = new ActiveWorkStatusEntity();
            activeWorkStatusEntity.setId(UUID.randomUUID());
            activeWorkStatusEntity.setOrganizationId(organizationId);
            activeWorkStatusEntity.setMemberId(memberId);
        }

        activeWorkStatusEntity.setLastUpdate(now);
        activeWorkStatusEntity.setActiveCircuitId(circuitId);

        activeWorkStatusRepository.save(activeWorkStatusEntity);

        MemberWorkStatusDto memberStatus = memberStatusCapability.getStatusOfMember(organizationId, memberId);

        teamCircuitOperator.notifyTeamOfMemberStatusUpdate(organizationId, memberId, now, nanoTime, memberStatus);

    }

    public void resolveWTFWithYay(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        pushResolveStatus(organizationId, memberId, now, nanoTime);
    }

    public void resolveWTFWithAbort(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        pushResolveStatus(organizationId, memberId, now, nanoTime);
    }

    @Transactional
    private void pushResolveStatus(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity != null && activeWorkStatusEntity.getActiveCircuitId() != null) {

            activeWorkStatusEntity.setActiveCircuitId(null);
            activeWorkStatusRepository.save(activeWorkStatusEntity);
        }

        MemberWorkStatusDto memberStatus = memberStatusCapability.getStatusOfMember(organizationId, memberId);

        teamCircuitOperator.notifyTeamOfMemberStatusUpdate(organizationId, memberId, now, nanoTime, memberStatus);

    }


    @Transactional
    public void pushMemberWorkStatus(IntentionEntity activeIntention, LocalDateTime now, Long nanoTime) {

        ActiveWorkStatusEntity workStatus = activeWorkStatusRepository.findByMemberId(activeIntention.getMemberId());

        if (workStatus == null) {
            workStatus = new ActiveWorkStatusEntity();
            workStatus.setId(UUID.randomUUID());
            workStatus.setMemberId(activeIntention.getMemberId());
            workStatus.setOrganizationId(activeIntention.getOrganizationId());
        }

        workStatus.setActiveTaskId(activeIntention.getTaskId());
        workStatus.setLastUpdate(activeIntention.getPosition());
        workStatus.setWorkingOn(activeIntention.getDescription());

        activeWorkStatusRepository.save(workStatus);

        MemberWorkStatusDto memberStatus = memberStatusCapability.getStatusOfMember(activeIntention.getOrganizationId(), activeIntention.getMemberId());

        teamCircuitOperator.notifyTeamOfMemberStatusUpdate(activeIntention.getOrganizationId(), activeIntention.getMemberId(), now, nanoTime, memberStatus);
    }

    public void updateOnlineStatus(UUID organizationId, UUID memberId, OnlineStatus onlineStatus) {

    }
}

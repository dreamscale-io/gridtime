package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.organization.CircuitJoinType;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import com.dreamscale.gridtime.core.capability.circuit.TeamCircuitOperator;
import com.dreamscale.gridtime.core.capability.circuit.WTFCircuitOperator;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusRepository;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusRepository;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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
    private MemberStatusManager memberStatusManager;

    @Autowired
    TeamCircuitOperator teamCircuitOperator;

    @Autowired
    GridClock gridClock;

    @Autowired
    WTFCircuitOperator wtfCircuitOperator;

    @Autowired
    EntityManager entityManager;

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

        entityManager.flush();

        TeamMemberDto memberStatus = memberStatusManager.getStatusOfMember(organizationId, memberId);

        //TODO remove below hack because of cacheing issue
        LearningCircuitDto circuitDto = wtfCircuitOperator.getCircuit(organizationId,
                circuitId);

        memberStatus.setActiveCircuit(circuitDto);
        if (memberStatus.getId().equals(circuitDto.getOwnerId())) {
            memberStatus.setActiveJoinType(CircuitJoinType.OWNER);
        } else {
            memberStatus.setActiveJoinType(CircuitJoinType.TEAM_MEMBER);
        }

        teamCircuitOperator.notifyTeamOfMemberStatusUpdate(organizationId, memberId, now, nanoTime, memberStatus);

    }

    @Transactional
    public void resolveWTFWithYay(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        pushResolveStatus(organizationId, memberId, now, nanoTime);
    }

    @Transactional
    public void resolveWTFWithCancel(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        pushResolveStatus(organizationId, memberId, now, nanoTime);
    }


    private void pushResolveStatus(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity != null && activeWorkStatusEntity.getActiveCircuitId() != null) {

            activeWorkStatusEntity.setActiveCircuitId(null);
            activeWorkStatusRepository.save(activeWorkStatusEntity);
        }

        entityManager.flush();

        TeamMemberDto memberStatus = memberStatusManager.getStatusOfMember(organizationId, memberId);

        //pulls from the cached version for this session, TODO is there a way to fix this caching thing?
        memberStatus.setActiveCircuit(null);
        memberStatus.setActiveJoinType(null);

        teamCircuitOperator.notifyTeamOfMemberStatusUpdate(organizationId, memberId, now, nanoTime, memberStatus);

    }

    public void pushTeamMemberStatusUpdate(UUID organizationId, UUID memberId, LocalDateTime now, Long nanoTime) {

        TeamMemberDto memberStatus = memberStatusManager.getStatusOfMember(organizationId, memberId);

        teamCircuitOperator.notifyTeamOfMemberStatusUpdate(organizationId, memberId, now, nanoTime, memberStatus);

    }
}

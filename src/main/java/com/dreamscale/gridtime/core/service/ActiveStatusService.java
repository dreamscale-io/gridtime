package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.organization.OnlineStatus;
import com.dreamscale.gridtime.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusRepository;
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusRepository;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ActiveStatusService {

    @Autowired
    ActiveWorkStatusRepository activeWorkStatusRepository;

    @Autowired
    TeamMemberWorkStatusRepository teamMemberWorkStatusRepository;

    @Autowired
    TeamCircuitOperator teamCircuitOperator;

    @Autowired
    TimeService timeService;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<TeamMemberWorkStatusDto, TeamMemberWorkStatusEntity> teamMemberStatusMapper;

    @PostConstruct
    private void init() {
        teamMemberStatusMapper = mapperFactory.createDtoEntityMapper(TeamMemberWorkStatusDto.class, TeamMemberWorkStatusEntity.class);
    }

    //rename circleId to circuitId

    //when team member pushes WTF, notify room
    //when team member resolves WTF, notify room
    //when team member updates their intention, notify room
    //when team member goes online/offline, notify room

    @Transactional
    public TeamMemberWorkStatusDto pushWTFStatus(UUID organizationId, UUID memberId, UUID circuitId) {

        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        LocalDateTime now = timeService.now();

        if (activeWorkStatusEntity == null) {
            activeWorkStatusEntity = new ActiveWorkStatusEntity();
            activeWorkStatusEntity.setId(UUID.randomUUID());
            activeWorkStatusEntity.setOrganizationId(organizationId);
            activeWorkStatusEntity.setMemberId(memberId);
        }

        activeWorkStatusEntity.setLastUpdate(now);
        activeWorkStatusEntity.setActiveCircuitId(circuitId);

        activeWorkStatusRepository.save(activeWorkStatusEntity);


        TeamMemberWorkStatusEntity myStatusEntity = teamMemberWorkStatusRepository.findOne(memberId);
        return teamMemberStatusMapper.toApi(myStatusEntity);

    }

    public TeamMemberWorkStatusDto resolveWTFWithYay(UUID organizationId, UUID memberId) {

        return pushResolveStatus(memberId, "Solved", "YAY!");
    }

    public TeamMemberWorkStatusDto resolveWTFWithAbort(UUID organizationId, UUID memberId) {

        return pushResolveStatus(memberId, "Aborted", "...");
    }

    @Transactional
    private TeamMemberWorkStatusDto pushResolveStatus(UUID memberId, String resolution, String newStatus) {
        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity != null && activeWorkStatusEntity.getActiveCircuitId() != null) {

            activeWorkStatusEntity.setActiveCircuitId(null);
            activeWorkStatusRepository.save(activeWorkStatusEntity);
        }

        TeamMemberWorkStatusEntity myStatusEntity = teamMemberWorkStatusRepository.findOne(memberId);
        return teamMemberStatusMapper.toApi(myStatusEntity);
    }


    @Transactional
    public void pushMemberWorkStatus(IntentionEntity activeIntention) {

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
    }

    public void updateOnlineStatus(UUID organizationId, UUID memberId, OnlineStatus onlineStatus) {

    }
}

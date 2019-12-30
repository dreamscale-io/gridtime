package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveWorkStatusRepository;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberWorkStatusRepository;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
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
    TimeService timeService;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<TeamMemberWorkStatusDto, TeamMemberWorkStatusEntity> teamMemberStatusMapper;

    @PostConstruct
    private void init() {
        teamMemberStatusMapper = mapperFactory.createDtoEntityMapper(TeamMemberWorkStatusDto.class, TeamMemberWorkStatusEntity.class);
    }



    public UUID getActiveCircleId(UUID organizationId, UUID memberId) {
        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        UUID activeCircle = null;
        if (activeWorkStatusEntity != null) {
            activeCircle = activeWorkStatusEntity.getActiveCircleId();
        }
        return activeCircle;
    }

    public TeamMemberWorkStatusDto resolveWTFWithYay(UUID organizationId, UUID memberId) {

        return updateActiveStatus(memberId, "Solved", "YAY!");
    }

    public TeamMemberWorkStatusDto resolveWTFWithAbort(UUID organizationId, UUID memberId) {

        return updateActiveStatus(memberId, "Aborted", "...");
    }

    private TeamMemberWorkStatusDto updateActiveStatus(UUID memberId, String resolution, String newStatus) {
        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity != null && activeWorkStatusEntity.getActiveCircleId() != null) {

            activeWorkStatusEntity.setActiveCircleId(null);
            activeWorkStatusRepository.save(activeWorkStatusEntity);
        }

        TeamMemberWorkStatusEntity myStatusEntity = teamMemberWorkStatusRepository.findOne(memberId);
        return teamMemberStatusMapper.toApi(myStatusEntity);
    }


    public TeamMemberWorkStatusDto pushWTFStatus(UUID organizationId, UUID memberId, UUID circuitId, String problemDescription) {

        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity == null) {
            activeWorkStatusEntity = new ActiveWorkStatusEntity();
            activeWorkStatusEntity.setId(UUID.randomUUID());
            activeWorkStatusEntity.setOrganizationId(organizationId);
            activeWorkStatusEntity.setMemberId(memberId);
        }

        activeWorkStatusEntity.setLastUpdate(timeService.now());
        activeWorkStatusEntity.setActiveCircleId(circuitId);

        activeWorkStatusRepository.save(activeWorkStatusEntity);

        TeamMemberWorkStatusEntity myStatusEntity = teamMemberWorkStatusRepository.findOne(memberId);
        return teamMemberStatusMapper.toApi(myStatusEntity);

    }

    public Long calculateAlarmDuration(LocalDateTime alarmTime) {

        Long durationInSeconds = null;

        if (alarmTime != null ) {
            Duration durationObj = Duration.between(alarmTime, timeService.now());

            durationInSeconds = durationObj.getSeconds();
        }

        return durationInSeconds;
    }



}

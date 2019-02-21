package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.status.WtfStatusInputDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class WTFService {

    @Autowired
    WtfSessionRepository wtfSessionRepository;

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


    public TeamMemberWorkStatusDto resolveWTFWithYay(UUID organizationId, UUID memberId) {

        return updateActiveStatus(memberId, "Solved", "YAY!");
    }

    public TeamMemberWorkStatusDto resolveWTFWithAbort(UUID organizationId, UUID memberId) {

        return updateActiveStatus(memberId, "Aborted", "...");
    }

    private TeamMemberWorkStatusDto updateActiveStatus(UUID memberId, String resolution, String newStatus) {
        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity != null && activeWorkStatusEntity.getActiveSessionId() != null) {
            WtfSessionEntity wtfSession = wtfSessionRepository.findOne(activeWorkStatusEntity.getActiveSessionId());

            wtfSession.setEndTime(timeService.now());
            wtfSession.setResolution(resolution);

            wtfSessionRepository.save(wtfSession);

            activeWorkStatusEntity.setSpiritStatus(newStatus);
            activeWorkStatusEntity.setSpiritMessage(null);
            activeWorkStatusEntity.setActiveSessionId(null);
            activeWorkStatusEntity.setActiveSessionStart(null);

            activeWorkStatusRepository.save(activeWorkStatusEntity);
        }

        TeamMemberWorkStatusEntity myStatusEntity = teamMemberWorkStatusRepository.findOne(memberId);
        return teamMemberStatusMapper.toApi(myStatusEntity);
    }


    public TeamMemberWorkStatusDto pushWTFStatus(UUID organizationId, UUID memberId, String problemDescription) {

        WtfSessionEntity wtfSession = new WtfSessionEntity();

        wtfSession.setId(UUID.randomUUID());
        wtfSession.setOrganizationId(organizationId);
        wtfSession.setMemberId(memberId);
        wtfSession.setStartTime(timeService.now());
        wtfSession.setProblemStatement(problemDescription);

        wtfSessionRepository.save(wtfSession);

        ActiveWorkStatusEntity activeWorkStatusEntity = activeWorkStatusRepository.findByMemberId(memberId);

        if (activeWorkStatusEntity != null) {
            String problemStatus = problemDescription;
            if (problemStatus == null) {
                problemStatus = "";
            }

            activeWorkStatusEntity.setActiveSessionId(wtfSession.getId());
            activeWorkStatusEntity.setActiveSessionStart(wtfSession.getStartTime());
            activeWorkStatusEntity.setSpiritStatus("WTF?!");
            activeWorkStatusEntity.setSpiritMessage(problemStatus);

            activeWorkStatusRepository.save(activeWorkStatusEntity);
        }

        TeamMemberWorkStatusEntity myStatusEntity = teamMemberWorkStatusRepository.findOne(memberId);
        TeamMemberWorkStatusDto myStatusDto = teamMemberStatusMapper.toApi(myStatusEntity);

        if (myStatusDto != null) {
            myStatusDto.setAlarmDurationInSeconds(calculateAlarmDuration(myStatusEntity.getActiveSessionStart()));
        }

        return myStatusDto;
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

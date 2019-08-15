package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.account.ActiveUserContextDto;
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.team.TeamDto;
import com.dreamscale.htmflow.core.domain.active.ActiveUserContextEntity;
import com.dreamscale.htmflow.core.domain.active.ActiveUserContextRepository;
import com.dreamscale.htmflow.core.domain.active.ActiveWorkStatusEntity;
import com.dreamscale.htmflow.core.domain.active.ActiveWorkStatusRepository;
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.domain.member.TeamMemberWorkStatusEntity;
import com.dreamscale.htmflow.core.domain.member.TeamMemberWorkStatusRepository;
import com.dreamscale.htmflow.core.hooks.realtime.dto.MemberInputDto;
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
public class ActiveUserContextService {

    @Autowired
    ActiveUserContextRepository activeUserContextRepository;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    TeamService teamService;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<ActiveUserContextDto, ActiveUserContextEntity> activeUserContextMapper;

    @PostConstruct
    private void init() {
        activeUserContextMapper = mapperFactory.createDtoEntityMapper(ActiveUserContextDto.class, ActiveUserContextEntity.class);
    }

    public ActiveUserContextDto getActiveUserContext(UUID masterAccountId) {

        ActiveUserContextEntity userContext = activeUserContextRepository.findByMasterAccountId(masterAccountId);

        if (userContext == null) {

            userContext = new ActiveUserContextEntity();
            userContext.setMasterAccountId(masterAccountId);

            OrganizationMemberEntity defaultMembership = organizationService.getDefaultMembership(masterAccountId);

            if (defaultMembership != null) {
                userContext.setOrganizationId(defaultMembership.getOrganizationId());
                userContext.setMemberId(defaultMembership.getId());

                TeamDto defaultTeam = teamService.getMyPrimaryTeam(defaultMembership.getOrganizationId(), defaultMembership.getId());
                if (defaultTeam != null) {
                    userContext.setTeamId(defaultTeam.getId());
                }

                activeUserContextRepository.save(userContext);
            }
        }

        return activeUserContextMapper.toApi(userContext);
    }


}

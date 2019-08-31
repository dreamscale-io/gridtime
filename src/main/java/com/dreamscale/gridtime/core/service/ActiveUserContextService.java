package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.account.UserContextDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.active.ActiveUserContextEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveUserContextRepository;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    private DtoEntityMapper<UserContextDto, ActiveUserContextEntity> activeUserContextMapper;

    @PostConstruct
    private void init() {
        activeUserContextMapper = mapperFactory.createDtoEntityMapper(UserContextDto.class, ActiveUserContextEntity.class);
    }

    public UserContextDto getActiveUserContext(UUID masterAccountId) {

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

package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.account.UserContextDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamDirectoryCapability;
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
    OrganizationMembershipCapability organizationMembership;

    @Autowired
    TeamDirectoryCapability teamDirectoryCapability;

    @Autowired
    private MapperFactory mapperFactory;

    private DtoEntityMapper<UserContextDto, ActiveUserContextEntity> activeUserContextMapper;

    @PostConstruct
    private void init() {
        activeUserContextMapper = mapperFactory.createDtoEntityMapper(UserContextDto.class, ActiveUserContextEntity.class);
    }

    public UserContextDto getActiveUserContext(UUID rootAccountId) {

        ActiveUserContextEntity userContext = activeUserContextRepository.findByRootAccountId(rootAccountId);

        if (userContext == null) {

            userContext = new ActiveUserContextEntity();
            userContext.setRootAccountId(rootAccountId);

            OrganizationMemberEntity defaultMembership = organizationMembership.getDefaultMembership(rootAccountId);

            if (defaultMembership != null) {
                userContext.setOrganizationId(defaultMembership.getOrganizationId());
                userContext.setMemberId(defaultMembership.getId());

                TeamDto defaultTeam = teamDirectoryCapability.getMyPrimaryTeam(defaultMembership.getOrganizationId(), defaultMembership.getId());
                if (defaultTeam != null) {
                    userContext.setTeamId(defaultTeam.getId());
                }

                activeUserContextRepository.save(userContext);
            }
        }

        return activeUserContextMapper.toApi(userContext);
    }


}

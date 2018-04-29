package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.organization.OrganizationInputDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ErrorEntity;
import org.dreamscale.exception.WebApplicationException;
import org.dreamscale.logging.LoggingLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationInviteTokenRepository inviteTokenRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private JiraConnectionFactory jiraConnectionFactory;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<OrganizationInputDto, OrganizationEntity> orgInputMapper;
    private DtoEntityMapper<OrganizationDto, OrganizationEntity> orgOutputMapper;

    @PostConstruct
    private void init() {
        orgInputMapper = mapperFactory.createDtoEntityMapper(OrganizationInputDto.class, OrganizationEntity.class);
        orgOutputMapper = mapperFactory.createDtoEntityMapper(OrganizationDto.class, OrganizationEntity.class);
    }

    public OrganizationDto createOrganization(OrganizationInputDto orgInputDto) {
        OrganizationEntity orgEntity = orgInputMapper.toEntity(orgInputDto);
        orgEntity.setId(UUID.randomUUID());
        orgEntity.setJiraSiteUrl(formatSiteUrl(orgInputDto.getJiraSiteUrl()));

        organizationRepository.save(orgEntity);

        OrganizationDto orgDto = new OrganizationDto();
        orgDto.setId(orgEntity.getId());
        orgDto.setName(orgEntity.getName());
        orgDto.setJiraSiteUrl(orgEntity.getJiraSiteUrl());

        try {
            jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());
            orgDto.setConnectionStatus(Status.VALID);
        } catch (Exception ex) {
            orgDto.setConnectionStatus(Status.FAILED);
            orgDto.setConnectionFailedMessage(ex.getMessage());
        }

        if (orgDto.getConnectionStatus() == Status.VALID) {
            OrganizationInviteTokenEntity inviteToken = new OrganizationInviteTokenEntity();
            inviteToken.setOrganizationId(orgDto.getId());
            inviteToken.setId(UUID.randomUUID());
            inviteToken.setToken(UUID.randomUUID().toString().replace("-", ""));
            inviteToken.setExpirationDate(LocalDateTime.now().plusWeeks(2));

            inviteTokenRepository.save(inviteToken);

            String inviteLink = constructInvitationLink(inviteToken.getToken());
            orgDto.setInviteLink(inviteLink);
            orgDto.setInviteToken(inviteToken.getToken());
        }

        return orgDto;
    }

    private String formatSiteUrl(String jiraSiteUrl) {
        String siteUrl;
        if (jiraSiteUrl.contains("//")) {
            String afterProtocol = jiraSiteUrl.substring(jiraSiteUrl.indexOf("//")+1);
            siteUrl = "https://"+afterProtocol;
        } else {
            siteUrl = "https://"+jiraSiteUrl;
        }
        return siteUrl;
    }

    private String constructInvitationLink(String inviteToken) {
        String baseInviteLink = ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH;
        return baseInviteLink + "?token="+inviteToken;
    }

    public OrganizationDto decodeInvitation(String inviteToken) {

        OrganizationDto organizationDto = null;

        OrganizationInviteTokenEntity inviteTokenEntity = inviteTokenRepository.findByToken(inviteToken);

        if (inviteTokenEntity != null) {
            OrganizationEntity orgEntity = organizationRepository.findById(inviteTokenEntity.getOrganizationId());
            organizationDto = orgOutputMapper.toApi(orgEntity);
            organizationDto.setInviteToken(inviteToken);
            organizationDto.setInviteLink(constructInvitationLink(inviteToken));
            organizationDto.setConnectionStatus(Status.VALID);

        } else {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "token invalid", null, LoggingLevel.WARN));
        }

        return organizationDto;
    }
}

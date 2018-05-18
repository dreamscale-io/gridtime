package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.*;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.JiraUserDto;
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
import java.util.ArrayList;
import java.util.List;
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
    private MasterAccountRepository masterAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository activeAccountStatusRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

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

        OrganizationEntity inputOrgEntity = orgInputMapper.toEntity(orgInputDto);
        ConnectionResult connectionResult = validateJiraConnection(inputOrgEntity);

        OrganizationDto outputDto = null;

        if (connectionResult.status == Status.FAILED) {
            outputDto = orgOutputMapper.toApi(inputOrgEntity);
            outputDto.setConnectionStatus(connectionResult.status);
            outputDto.setConnectionFailedMessage(connectionResult.errorMessage);
        } else {
            outputDto = findOrCreateOrganization(orgInputDto);
            outputDto.setConnectionStatus(connectionResult.status);
        }

        return outputDto;
    }

    private OrganizationDto findOrCreateOrganization(OrganizationInputDto inputDto) {
        OrganizationDto outputOrg;

        OrganizationEntity existingOrg = organizationRepository.findByDomainName(inputDto.getDomainName());

        if (existingOrg != null) {

            OrganizationInviteTokenEntity inviteToken = inviteTokenRepository.findByOrganizationId(existingOrg.getId());

            outputOrg = orgOutputMapper.toApi(existingOrg);
            outputOrg.setConnectionStatus(Status.VALID);
            outputOrg.setInviteLink(constructInvitationLink(inviteToken.getToken()));
            outputOrg.setInviteToken(inviteToken.getToken());

        } else {

            OrganizationEntity orgEntity = orgInputMapper.toEntity(inputDto);
            orgEntity.setId(UUID.randomUUID());

            organizationRepository.save(orgEntity);

            OrganizationInviteTokenEntity inviteToken = createInviteToken(orgEntity.getId());
            inviteTokenRepository.save(inviteToken);

            outputOrg = orgOutputMapper.toApi(orgEntity);
            outputOrg.setInviteLink(constructInvitationLink(inviteToken.getToken()));
            outputOrg.setInviteToken(inviteToken.getToken());
        }
        return outputOrg;
    }

    private OrganizationInviteTokenEntity createInviteToken(UUID organizationId) {
            OrganizationInviteTokenEntity inviteToken = new OrganizationInviteTokenEntity();
            inviteToken.setOrganizationId(organizationId);
            inviteToken.setId(UUID.randomUUID());
            inviteToken.setToken(generateToken());
            inviteToken.setExpirationDate(LocalDateTime.now().plusWeeks(2));

        return inviteToken;
    }

    private ConnectionResult validateJiraConnection(OrganizationEntity orgEntity) {
        ConnectionResult result = new ConnectionResult();

        if (jiraUserNotInOrgDomain(orgEntity.getDomainName(), orgEntity.getJiraUser())) {
            result.status = Status.FAILED;
            result.errorMessage = "Jira user not in organization domain";
        } else {
            try {
                JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());
                connection.validate();
                result.status = Status.VALID;
            } catch (Exception ex) {
                result.status = Status.FAILED;
                result.errorMessage = "Failed to connect to Jira";
                //result.errorMessage = ex.getMessage();
            }
        }

        return result;
    }

    private boolean jiraUserNotInOrgDomain(String domainName, String jiraUser) {
        return domainName == null || jiraUser == null || !jiraUser.toLowerCase().endsWith(domainName.toLowerCase());
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }



    private String constructInvitationLink(String inviteToken) {
        String baseInviteLink = ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH;
        return baseInviteLink + "?token=" + inviteToken;
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
            throw new WebApplicationException(404, new ErrorEntity("404", null, "token not found", null, LoggingLevel.WARN));
        }

        return organizationDto;
    }

    public MembershipDetailsDto registerMember(UUID organizationId, MembershipInputDto membershipInputDto) {

        //if invitation is invalid, this will throw a 404
        OrganizationDto organizationDto = decodeInvitation(membershipInputDto.getInviteToken());

        if (!organizationDto.getId().equals(organizationId)) {
            throw new WebApplicationException(404, new ErrorEntity("404", null,
                    "invitation token doesn't match organization", null, LoggingLevel.WARN));
        }

        OrganizationEntity orgEntity = organizationRepository.findById(organizationDto.getId());

        //if user is invalid, this will throw a 404
        JiraUserDto jiraUser = lookupMatchingJiraUser(orgEntity, membershipInputDto.getOrgEmail());

        MasterAccountEntity masterAccountEntity = new MasterAccountEntity();
        masterAccountEntity.setId(UUID.randomUUID());
        masterAccountEntity.setFullName(jiraUser.getDisplayName());
        masterAccountEntity.setMasterEmail(jiraUser.getEmailAddress());
        masterAccountEntity.setActivationCode(generateToken());

        masterAccountRepository.save(masterAccountEntity);

        OrganizationMemberEntity memberEntity = new OrganizationMemberEntity();
        memberEntity.setId(UUID.randomUUID());
        memberEntity.setOrganizationId(orgEntity.getId());
        memberEntity.setEmail(jiraUser.getEmailAddress());
        memberEntity.setExternalId(jiraUser.getAccountId());
        memberEntity.setMasterAccountId(masterAccountEntity.getId());

        memberRepository.save(memberEntity);

        ActiveAccountStatusEntity accountStatusEntity = new ActiveAccountStatusEntity();
        accountStatusEntity.setMasterAccountId(masterAccountEntity.getId());
        accountStatusEntity.setActiveStatus(ActiveAccountStatus.Offline);

        activeAccountStatusRepository.save(accountStatusEntity);


        MembershipDetailsDto membership = new MembershipDetailsDto();
        membership.setMemberId(memberEntity.getId());
        membership.setOrgEmail(memberEntity.getEmail());
        membership.setMasterAccountId(masterAccountEntity.getId());
        membership.setFullName(masterAccountEntity.getFullName());
        membership.setActivationCode(masterAccountEntity.getActivationCode());

        return membership;
    }


    private JiraUserDto lookupMatchingJiraUser(OrganizationEntity orgEntity, String email) {
        JiraConnection jiraConnection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());

        List<JiraUserDto> jiraUsers = jiraConnection.getUsers();

        JiraUserDto selectedUser = null;
        for (JiraUserDto jiraUser : jiraUsers) {
            String jiraEmail = jiraUser.getEmailAddress();
            if (jiraEmail != null && jiraEmail.equalsIgnoreCase(email)) {
                selectedUser = jiraUser;
            }
        }

        if (selectedUser == null) {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "user not found", null, LoggingLevel.WARN));
        }

        return selectedUser;
    }

    public OrganizationDto getDefaultOrganization(UUID masterAccountId) {
        List<OrganizationMemberEntity> orgMemberships = memberRepository.findByMasterAccountId(masterAccountId);

        if (orgMemberships == null || orgMemberships.size() == 0) {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "default organization not found", null, LoggingLevel.ERROR));
        }

        OrganizationEntity organizationEntity = organizationRepository.findById(orgMemberships.get(0).getOrganizationId());
        return orgOutputMapper.toApi(organizationEntity);
    }


    public List<OrgMemberStatusDto> getMembersForOrganization(UUID organizationId, UUID masterAccountId) {

        List<OrgMemberStatusDto> orgMemberDtos = new ArrayList<>();

        //first, need to figure out what org the user is part of, later on we can set the active org

        OrganizationMemberEntity orgMembership = memberRepository.findByOrganizationIdAndMasterAccountId(organizationId, masterAccountId);

        if (orgMembership != null) {

            List<MemberStatusEntity> peersAndMe = memberStatusRepository.findByOrganizationId(organizationId);

            //"select o.id, o.email, o.full_name, a.last_activity, a.active_status " +

            for (MemberStatusEntity memberStatusEntity : peersAndMe) {
                OrgMemberStatusDto orgMemberDto = OrgMemberStatusDto.builder()
                        .memberId(memberStatusEntity.getId())
                        .email(memberStatusEntity.getEmail())
                        .fullName(memberStatusEntity.getFullName())
                        .lastActivity(memberStatusEntity.getLastActivity())
                        .memberStatus(memberStatusEntity.getActiveStatus().name())
                        .build();

                orgMemberDtos.add(orgMemberDto);
            }

        } else {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "members not found", null, LoggingLevel.WARN));
        }

        return orgMemberDtos;
    }

    private class ConnectionResult {
        Status status;
        String errorMessage;
    }
}

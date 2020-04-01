package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.status.ConnectionResultDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.integration.JiraCapability;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraUserDto;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrganizationMembershipCapability {
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OrganizationInviteTokenRepository inviteTokenRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private RootAccountRepository rootAccountRepository;

    @Autowired
    private ActiveAccountStatusRepository activeAccountStatusRepository;

    @Autowired
    private TeamMembershipCapability teamMembershipCapability;


    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<OrganizationInputDto, OrganizationEntity> orgInputMapper;
    private DtoEntityMapper<OrganizationDto, OrganizationEntity> orgOutputMapper;

    @PostConstruct
    private void init() {
        orgInputMapper = mapperFactory.createDtoEntityMapper(OrganizationInputDto.class, OrganizationEntity.class);
        orgOutputMapper = mapperFactory.createDtoEntityMapper(OrganizationDto.class, OrganizationEntity.class);
    }

    public OrganizationDto getOrganizationByDomainName(String domainName) {
        OrganizationEntity orgEntity = organizationRepository.findByDomainName(domainName);
        return orgOutputMapper.toApi(orgEntity);
    }

    public OrganizationDto createOrganization(OrganizationInputDto orgInputDto) {

        OrganizationEntity inputOrgEntity = orgInputMapper.toEntity(orgInputDto);
        ConnectionResultDto connectionResult = jiraCapability.validateJiraConnection(inputOrgEntity);

        OrganizationDto outputDto = null;

        if (connectionResult.getStatus() == Status.FAILED) {
            outputDto = orgOutputMapper.toApi(inputOrgEntity);
            outputDto.setConnectionStatus(connectionResult.getStatus());
            outputDto.setConnectionFailedMessage(connectionResult.getMessage());
        } else {
            outputDto = findOrCreateOrganization(orgInputDto);
            outputDto.setConnectionStatus(connectionResult.getStatus());
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

            teamMembershipCapability.createEveryoneTeam(orgEntity.getId());

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
            throw new BadRequestException(ValidationErrorCodes.INVALID_OR_EXPIRED_INVITE_TOKEN, "Token not found");
        }

        return organizationDto;
    }

    public MemberRegistrationDetailsDto registerMember(UUID organizationId, MembershipInputDto membershipInputDto) {

        //if invitation is invalid, this will throw a 404
        OrganizationDto organizationDto = decodeInvitation(membershipInputDto.getInviteToken());

        if (!organizationDto.getId().equals(organizationId)) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Invitation token doesn't match organization");
        }

        OrganizationEntity orgEntity = organizationRepository.findById(organizationDto.getId());

        //if user is invalid, this will throw a 404
        JiraUserDto jiraUser = jiraCapability.getUserByEmail(orgEntity.getId(), membershipInputDto.getOrgEmail());

        RootAccountEntity rootAccountEntity = new RootAccountEntity();
        rootAccountEntity.setId(UUID.randomUUID());
        rootAccountEntity.setFullName(jiraUser.getDisplayName());
        rootAccountEntity.setRootEmail(jiraUser.getEmailAddress());
        rootAccountEntity.setActivationCode(generateToken());

        rootAccountRepository.save(rootAccountEntity);

        OrganizationMemberEntity memberEntity = new OrganizationMemberEntity();
        memberEntity.setId(UUID.randomUUID());
        memberEntity.setOrganizationId(orgEntity.getId());
        memberEntity.setEmail(jiraUser.getEmailAddress());
        memberEntity.setExternalId(jiraUser.getAccountId());
        memberEntity.setRootAccountId(rootAccountEntity.getId());

        memberRepository.save(memberEntity);

        teamMembershipCapability.addMemberToEveryone(orgEntity.getId(), memberEntity.getId());

        ActiveAccountStatusEntity accountStatusEntity = new ActiveAccountStatusEntity();
        accountStatusEntity.setRootAccountId(rootAccountEntity.getId());
        accountStatusEntity.setOnlineStatus(OnlineStatus.Offline);

        activeAccountStatusRepository.save(accountStatusEntity);


        MemberRegistrationDetailsDto membership = new MemberRegistrationDetailsDto();
        membership.setMemberId(memberEntity.getId());
        membership.setOrgEmail(memberEntity.getEmail());
        membership.setRootAccountId(rootAccountEntity.getId());
        membership.setFullName(rootAccountEntity.getFullName());
        membership.setActivationCode(rootAccountEntity.getActivationCode());

        return membership;
    }


    public OrganizationMemberEntity getDefaultMembership(UUID rootAccountId) {
        List<OrganizationMemberEntity> orgMemberships = memberRepository.findByRootAccountId(rootAccountId);

        if (orgMemberships == null || orgMemberships.size() == 0) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "organization membership not found");
        }

        return orgMemberships.get(0);
    }

    public OrganizationDto getDefaultOrganization(UUID rootAccountId) {
        List<OrganizationMemberEntity> orgMemberships = memberRepository.findByRootAccountId(rootAccountId);

        if (orgMemberships == null || orgMemberships.size() == 0) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "organization membership not found");
        }

        OrganizationEntity organizationEntity = organizationRepository.findById(orgMemberships.get(0).getOrganizationId());
        return orgOutputMapper.toApi(organizationEntity);
    }

    public OrganizationDto getDefaultOrganizationWithInvitation(UUID rootAccountId) {
        List<OrganizationMemberEntity> orgMemberships = memberRepository.findByRootAccountId(rootAccountId);

        if (orgMemberships == null || orgMemberships.size() == 0) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "organization membership not found");
        }

        OrganizationEntity organizationEntity = organizationRepository.findById(orgMemberships.get(0).getOrganizationId());

        OrganizationInviteTokenEntity inviteToken = inviteTokenRepository.findByOrganizationId(organizationEntity.getId());

        OrganizationDto outputOrg = orgOutputMapper.toApi(organizationEntity);
        outputOrg.setConnectionStatus(Status.VALID);
        outputOrg.setInviteLink(constructInvitationLink(inviteToken.getToken()));
        outputOrg.setInviteToken(inviteToken.getToken());

        return outputOrg;
    }

    public void validateMemberWithinOrgByMemberId(UUID organizationId, UUID memberId) {
        if (memberId == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }

        OrganizationMemberEntity otherMember = organizationMemberRepository.findById(memberId);
        if (otherMember == null || !otherMember.getOrganizationId().equals(organizationId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }
    }

    public void validateMemberWithinOrg(UUID organizationId, UUID rootAccountId) {
        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        if (membership == null || !membership.getOrganizationId().equals(organizationId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }
    }


    public UUID getMemberIdForUser(UUID organizationId, String userName) {
        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndUsername(organizationId, userName);

        UUID memberId = null;
        if (membership != null) {
            return membership.getId();
        }

        return null;
    }

    public String getUsernameForMemberId(UUID memberId) {
        OrganizationMemberEntity membership = organizationMemberRepository.findById(memberId);

        String userName = null;
        if (membership != null) {
            return membership.getUsername();
        }

        return userName;
    }
}

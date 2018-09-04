package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.AccountActivationDto
import com.dreamscale.htmflow.api.account.ActivationCodeDto
import com.dreamscale.htmflow.api.organization.MembershipInputDto
import com.dreamscale.htmflow.api.organization.OrgMemberStatusDto
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.htmflow.api.status.ConnectionResultDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.client.AccountClient
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.MasterAccountRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.htmflow.core.service.JiraService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class OrganizationResourceSpec extends Specification {

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    AccountClient accountClient

	@Autowired
	OrganizationRepository organizationRepository

	@Autowired
	MasterAccountRepository masterAccountRepository

    @Autowired
    JiraService mockJiraService

    @Autowired
    MasterAccountEntity testUser;

	def setup() {
		organizationRepository.deleteAll()
		masterAccountRepository.deleteAll()
	}

    def "should not create organization with failed Jira connect"() {
        given:
        OrganizationInputDto organization = createOrganizationWithInvalidJira();

        when:
        OrganizationDto organizationDto = organizationClient.createOrganization(organization)

        then:
        assert organizationDto != null
        assert organizationDto.getId() == null
        assert organizationDto.getOrgName() == organization.getOrgName()
        assert organizationDto.getConnectionStatus() == Status.FAILED
        assert organizationDto.getConnectionFailedMessage() != null
        assert organizationDto.getInviteLink() == null
    }

    def "should create organization with valid connection"() {
        given:

        OrganizationInputDto organization = createValidOrganization()

        when:
        OrganizationDto organizationDto = organizationClient.createOrganization(organization)

        then:
        assert organizationDto != null
        assert organizationDto.getId() != null
        assert organizationDto.getOrgName() == organization.getOrgName()
        assert organizationDto.getDomainName() == organization.getDomainName()
        assert organizationDto.getConnectionStatus() == Status.VALID
        assert organizationDto.getInviteLink() != null
    }

    def "should decode member invitation from link"() {
        given:
        OrganizationInputDto organization = createValidOrganization()

        when:
        OrganizationDto organizationDto = organizationClient.createOrganization(organization)
        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken());

        then:
        assert inviteOrg != null
        assert inviteOrg.getId() != null
        assert inviteOrg.getOrgName() == organization.getOrgName()
        assert inviteOrg.getDomainName() == organization.getDomainName()
        assert inviteOrg.getConnectionStatus() == Status.VALID
        assert inviteOrg.getInviteLink() != null
    }

    def "should add member to organization if valid user"() {
        given:
        OrganizationInputDto organization = createValidOrganization()

        OrganizationDto organizationDto = organizationClient.createOrganization(organization)
        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken());

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail("janelle@dreamscale.io")

        JiraUserDto jiraUserDto = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build();
        mockJiraService.getUserByEmail(_, _) >> jiraUserDto

        when:

        MemberRegistrationDetailsDto membershipDto = organizationClient.registerMember(inviteOrg.getId().toString(), membershipInputDto)

        then:
        assert membershipDto != null
        assert membershipDto.getMemberId() != null
        assert membershipDto.getMasterAccountId() != null
        assert membershipDto.getOrgEmail() == membershipInputDto.getOrgEmail()
        assert membershipDto.getFullName() == jiraUserDto.displayName
        assert membershipDto.getActivationCode() != null

    }

    def "should retrieve all members in organization"() {
        given:
        OrganizationInputDto organization = createValidOrganization()

        OrganizationDto organizationDto = organizationClient.createOrganization(organization)
        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken());

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail("janelle@dreamscale.io")
        JiraUserDto janelleUser = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build();
        mockJiraService.getUserByEmail(_, _) >> janelleUser

        MemberRegistrationDetailsDto detailsDto = organizationClient.registerMember(inviteOrg.getId().toString(), membershipInputDto)

        ActivationCodeDto activationCode = new ActivationCodeDto();
        activationCode.setActivationCode(detailsDto.getActivationCode());

        AccountActivationDto activationDto = accountClient.activate(activationCode);

        MasterAccountEntity masterAccountEntity = masterAccountRepository.findByApiKey(activationDto.apiKey);

        //Can only retrieve org members for orgs you are a part of,
        testUser.setApiKey(masterAccountEntity.getApiKey());
        testUser.setId(masterAccountEntity.getId())

        membershipInputDto.setOrgEmail("kara@dreamscale.io")
        JiraUserDto karaUser = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build();
        mockJiraService.getUserByEmail(_, _) >> karaUser

        organizationClient.registerMember(inviteOrg.getId().toString(), membershipInputDto)

        when:
        List<OrgMemberStatusDto> orgMembers = organizationClient.getMembers(inviteOrg.getId().toString())

        then:
        assert orgMembers != null
        assert orgMembers.size() == 2
    }



    private OrganizationInputDto createValidOrganization() {
        OrganizationInputDto organization = new OrganizationInputDto();
        organization.setOrgName("DreamScale")
        organization.setDomainName("dreamscale.io")
        organization.setJiraUser("janelle@dreamscale.io")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("9KC0iM24tfXf8iKDVP2q4198")

        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.VALID, null)

        return organization;
    }

    private OrganizationInputDto createOrganizationWithInvalidJira() {
        OrganizationInputDto organization = new OrganizationInputDto();
        organization.setOrgName("DreamScale")
        organization.setJiraUser("fake")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("blabla")

        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.FAILED, "failed")

        return organization;
    }

}

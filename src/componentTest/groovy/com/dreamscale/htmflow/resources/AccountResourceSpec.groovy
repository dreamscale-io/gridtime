package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.ActivationCodeDto
import com.dreamscale.htmflow.api.account.AccountActivationDto
import com.dreamscale.htmflow.api.account.ConnectionStatusDto
import com.dreamscale.htmflow.api.account.HeartbeatDto
import com.dreamscale.htmflow.api.account.SimpleStatusDto
import com.dreamscale.htmflow.api.organization.MembershipDetailsDto
import com.dreamscale.htmflow.api.organization.MembershipInputDto
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.status.ConnectionResultDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.client.AccountClient
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.MasterAccountRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.htmflow.core.service.JiraService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class AccountResourceSpec extends Specification {

    @Autowired
    AccountClient accountClient
    @Autowired
    OrganizationClient organizationClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    MasterAccountRepository masterAccountRepository

    @Autowired
    JiraService mockJiraService

    def setup() {
        organizationRepository.deleteAll()
        masterAccountRepository.deleteAll()
    }

    def "should activate account and create APIKey"() {
        given:

        OrganizationInputDto organization = createValidOrganization()
        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.VALID, null)

        OrganizationDto organizationDto = organizationClient.createOrganization(organization)

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail("janelle@dreamscale.io")

        JiraUserDto janelleUser = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build();
        mockJiraService.getUserByEmail(_, _) >> janelleUser

        MembershipDetailsDto membershipDto = organizationClient.registerMember(organizationDto.getId().toString(), membershipInputDto)

        ActivationCodeDto activationCode = new ActivationCodeDto();
        activationCode.setActivationCode(membershipDto.getActivationCode());

        when:

        AccountActivationDto activationDto = accountClient.activate(activationCode);

        then:
        assert activationDto != null
        assert activationDto.apiKey != null
        assert activationDto.email == membershipDto.orgEmail
    }

    def "should login"() {
        given:

        when:
        ConnectionStatusDto connectionStatusDto = accountClient.login()

        then:
        assert connectionStatusDto != null
        assert connectionStatusDto.getConnectionId() != null;
        assert connectionStatusDto.status == Status.VALID
    }

    def "should logout"() {
        given:

        when:
        SimpleStatusDto statusDto = accountClient.logout()

        then:
        assert statusDto != null
        assert statusDto.status == Status.VALID
    }

    def "should update heartbeat"() {
        given:
        HeartbeatDto heartbeatDto = new HeartbeatDto();
        accountClient.login();

        when:

        SimpleStatusDto statusDto = accountClient.heartbeat(heartbeatDto)

        then:
        assert statusDto != null
        assert statusDto.status == Status.VALID
    }

    private OrganizationInputDto createValidOrganization() {
        OrganizationInputDto organization = new OrganizationInputDto();
        organization.setOrgName("DreamScale")
        organization.setDomainName("dreamscale.io")
        organization.setJiraUser("janelle@dreamscale.io")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("9KC0iM24tfXf8iKDVP2q4198")

        return organization;
    }

}

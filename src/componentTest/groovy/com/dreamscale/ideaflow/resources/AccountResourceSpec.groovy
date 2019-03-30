package com.dreamscale.ideaflow.resources

import com.dreamscale.ideaflow.ComponentTest
import com.dreamscale.ideaflow.api.account.ActivationCodeDto
import com.dreamscale.ideaflow.api.account.AccountActivationDto
import com.dreamscale.ideaflow.api.account.ConnectionStatusDto
import com.dreamscale.ideaflow.api.account.HeartbeatDto
import com.dreamscale.ideaflow.api.account.SimpleStatusDto
import com.dreamscale.ideaflow.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.ideaflow.api.organization.MembershipInputDto
import com.dreamscale.ideaflow.api.organization.OrganizationDto
import com.dreamscale.ideaflow.api.organization.OrganizationInputDto
import com.dreamscale.ideaflow.api.status.ConnectionResultDto
import com.dreamscale.ideaflow.api.status.Status
import com.dreamscale.ideaflow.client.AccountClient
import com.dreamscale.ideaflow.client.OrganizationClient
import com.dreamscale.ideaflow.core.domain.MasterAccountRepository
import com.dreamscale.ideaflow.core.domain.OrganizationRepository
import com.dreamscale.ideaflow.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.ideaflow.core.service.JiraService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

@ComponentTest
class AccountResourceSpec extends Specification {

    @Autowired
    AccountClient accountClient
    @Autowired
    AccountClient unauthenticatedAccountClient
    @Autowired
    OrganizationClient organizationClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    MasterAccountRepository masterAccountRepository

    @Autowired
    JiraService mockJiraService

    def "should activate account and create APIKey"() {
        given:
        OrganizationInputDto organization = createValidOrganization()
        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.VALID, null)

        OrganizationDto organizationDto = organizationClient.createOrganization(organization)

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail("janelle@dreamscale.io")

        JiraUserDto janelleUser = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build()
        mockJiraService.getUserByEmail(_, _) >> janelleUser

        MemberRegistrationDetailsDto membershipDto = organizationClient.registerMember(organizationDto.getId().toString(), membershipInputDto)

        ActivationCodeDto activationCode = new ActivationCodeDto()
        activationCode.setActivationCode(membershipDto.getActivationCode())

        when:
        AccountActivationDto activationDto = unauthenticatedAccountClient.activate(activationCode)

        then:
        assert activationDto != null
        assert activationDto.apiKey != null
        assert activationDto.email == membershipDto.orgEmail
    }

    def "should login"() {
        when:
        ConnectionStatusDto connectionStatusDto = accountClient.login()

        then:
        assert connectionStatusDto != null
        assert connectionStatusDto.getConnectionId() != null
        assert connectionStatusDto.status == Status.VALID
    }

    def "should logout"() {
        when:
        SimpleStatusDto statusDto = accountClient.logout()

        then:
        assert statusDto != null
        assert statusDto.status == Status.VALID
    }

    def "should update heartbeat"() {
        given:
        HeartbeatDto heartbeatDto = new HeartbeatDto()
        accountClient.login()

        when:
        SimpleStatusDto statusDto = accountClient.heartbeat(heartbeatDto)

        then:
        assert statusDto != null
        assert statusDto.status == Status.VALID
    }

    private OrganizationInputDto createValidOrganization() {
        OrganizationInputDto organization = new OrganizationInputDto()
        organization.setOrgName("DreamScale")
        organization.setDomainName("dreamscale.io")

        return organization
    }

}

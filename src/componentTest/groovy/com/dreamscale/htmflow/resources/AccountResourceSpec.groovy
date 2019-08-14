package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.account.ActivationCodeDto
import com.dreamscale.htmflow.api.account.AccountActivationDto
import com.dreamscale.htmflow.api.account.ConnectionStatusDto
import com.dreamscale.htmflow.api.account.HeartbeatDto
import com.dreamscale.htmflow.api.account.SimpleStatusDto
import com.dreamscale.htmflow.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.htmflow.api.organization.MembershipInputDto
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.status.ConnectionResultDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.client.AccountClient
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.member.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.member.MasterAccountRepository
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationRepository
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
    AccountClient unauthenticatedAccountClient
    @Autowired
    OrganizationClient organizationClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    MasterAccountRepository masterAccountRepository

    @Autowired
    MasterAccountEntity testUser

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

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(testUser.id).save()

        ConnectionStatusDto connectionStatusDto = accountClient.login()

        then:
        assert connectionStatusDto != null
        assert connectionStatusDto.organizationId != null
        assert connectionStatusDto.memberId != null
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

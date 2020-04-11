package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ConnectionInputDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.DisplayNameInputDto
import com.dreamscale.gridtime.api.account.EmailInputDto
import com.dreamscale.gridtime.api.account.FullNameInputDto
import com.dreamscale.gridtime.api.account.HeartbeatDto
import com.dreamscale.gridtime.api.account.RoomConnectionScopeDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserNameInputDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.gridtime.api.organization.MembershipInputDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationInputDto
import com.dreamscale.gridtime.api.status.ConnectionResultDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class AccountResourceSpec extends Specification {

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    AccountClient accountClient
    @Autowired
    AccountClient unauthenticatedAccountClient
    @Autowired
    OrganizationClient organizationClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    RootAccountRepository masterAccountRepository

    @Autowired
    RootAccountEntity testUser

    @Autowired
    JiraCapability mockJiraService

    def "should activate account and create APIKey"() {
        given:
        OrganizationInputDto organization = createValidOrganization()
        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.SUCCESS, null)

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

    //25050596-d768-4492-b8e5-256a3c05fe1f - orgId


    def "should login"() {

        when:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        ConnectionStatusDto connectionStatusDto = accountClient.login()

        then:
        assert connectionStatusDto != null
        assert connectionStatusDto.organizationId != null
        assert connectionStatusDto.memberId != null
        assert connectionStatusDto.status == Status.SUCCESS
    }

    def "on talk connect should resume rooms"() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        when:

        LearningCircuitDto circuitDto = circuitClient.startWTF()

        ConnectionStatusDto connectionStatusDto = accountClient.login()

        RoomConnectionScopeDto roomConnections = accountClient.connect(new ConnectionInputDto(connectionStatusDto.getConnectionId()));

        then:
        assert roomConnections != null
        assert roomConnections.roomIdsToJoin.size() == 2
    }

    def "should create a circuit then logout & login again"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        when:

        ConnectionStatusDto connectionStatusDto = accountClient.login()

        LearningCircuitDto circuitDto = circuitClient.startWTF()

        SimpleStatusDto logoutStatus = accountClient.logout()
        ConnectionStatusDto newConnectionStatus = accountClient.login()


        then:
        assert newConnectionStatus != null
        assert newConnectionStatus.connectionId != connectionStatusDto.connectionId
        assert newConnectionStatus.organizationId == member.getOrganizationId()
        assert newConnectionStatus.memberId == member.id
        assert newConnectionStatus.status == Status.SUCCESS
    }

    def "should update profile properties"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        when:

        UserProfileDto profile = accountClient.updateProfileUserName(new UserNameInputDto("joeblow"))

        profile = accountClient.updateProfileDisplayName(new DisplayNameInputDto("Joe"))
        profile = accountClient.updateProfileFullName(new FullNameInputDto("Joe Blow"))
        profile = accountClient.updateProfileEmail(new EmailInputDto("joe@blow.com"))

        then:
        assert profile != null
        assert profile.getRootId() != null
        assert profile.getDisplayName() == "Joe"
        assert profile.getFullName() == "Joe Blow"
        assert profile.getUserName() == "joeblow"
        assert profile.getEmail() == "joe@blow.com"
    }

    def "should logout"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        when:
        SimpleStatusDto statusDto = accountClient.logout()

        then:
        assert statusDto != null
        assert statusDto.status == Status.SUCCESS
    }

    def "should update heartbeat"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        HeartbeatDto heartbeatDto = new HeartbeatDto()
        heartbeatDto.setDeltaTime(30);

        ConnectionStatusDto connect = accountClient.login()
        accountClient.connect(new ConnectionInputDto(connect.getConnectionId()));

        when:
        SimpleStatusDto statusDto = accountClient.heartbeat(heartbeatDto)

        then:
        assert statusDto != null
        assert statusDto.status == Status.SUCCESS
    }

    private OrganizationInputDto createValidOrganization() {
        OrganizationInputDto organization = new OrganizationInputDto()
        organization.setOrgName("DreamScale")
        organization.setDomainName("dreamscale.io")

        return organization
    }


    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationEntity org = aRandom.organizationEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }
}

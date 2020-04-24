package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActiveTalkConnectionDto
import com.dreamscale.gridtime.api.account.ConnectionInputDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.DisplayNameInputDto
import com.dreamscale.gridtime.api.account.EmailInputDto
import com.dreamscale.gridtime.api.account.FullNameInputDto
import com.dreamscale.gridtime.api.account.HeartbeatDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserNameInputDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.organization.OrganizationInputDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

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

    @Autowired
    EmailCapability mockEmailCapability;

    @Autowired
    GridClock mockTimeService

    String activationToken = null;

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
        mockTimeService.nanoTime() >> System.nanoTime()
    }

    def "should activate account and create APIKey"() {
        given:
        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail("janelle@dreamscale.io")

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { email, token -> activationToken = token; return null}

        SimpleStatusDto registerStatus = accountClient.register(rootAccountInput)

        when:

        AccountActivationDto activationDto = accountClient.activate(new ActivationCodeDto(activationToken))

        then:

        assert registerStatus.status == Status.SUCCESS

        assert activationDto != null
        assert activationDto.apiKey != null
        assert activationDto.email == rootAccountInput.email
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
        assert connectionStatusDto.status == Status.VALID
    }

    def "on talk connect should resume rooms"() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        ConnectionStatusDto firstLogin = accountClient.login()

        LearningCircuitDto circuit = circuitClient.startWTF()

        SimpleStatusDto logoutStatus = accountClient.logout()

        when:

        ConnectionStatusDto secondLogin = accountClient.login()

        ActiveTalkConnectionDto activeTalkConnection = accountClient.connect(new ConnectionInputDto(secondLogin.getConnectionId()));

        then:
        assert activeTalkConnection != null
        assert activeTalkConnection.getStatus() == Status.VALID
        assert activeTalkConnection.getActiveRooms().size() == 3
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
        assert newConnectionStatus.status == Status.VALID
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
        assert profile.getEmail() == "joe@blow.com (pending validation)"
    }

    def "should logout"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam()
        testUser.setId(member.getRootAccountId())

        when:

        ConnectionStatusDto loginStatus = accountClient.login()
        SimpleStatusDto logoutStatus = accountClient.logout()

        then:
        assert loginStatus != null
        assert loginStatus.status == Status.VALID

        assert logoutStatus != null
        assert logoutStatus.status == Status.SUCCESS
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

package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.organization.MemberWorkStatusDto
import com.dreamscale.gridtime.api.organization.MembershipInputDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationInputDto
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.gridtime.api.organization.TeamMemberWorkStatusDto
import com.dreamscale.gridtime.api.organization.TeamWithMembersDto
import com.dreamscale.gridtime.api.status.ConnectionResultDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.api.team.TeamInputDto
import com.dreamscale.gridtime.api.team.TeamMemberDto
import com.dreamscale.gridtime.api.team.TeamMembersToAddInputDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberRepository
import com.dreamscale.gridtime.core.domain.member.TeamRepository
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class OrganizationResourceSpec extends Specification {

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    TeamClient teamClient

    @Autowired
    AccountClient accountClient

	@Autowired
	OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
	RootAccountRepository masterAccountRepository

    @Autowired
    TeamRepository teamRepository

    @Autowired
    TeamMemberRepository teamMemberRepository

    @Autowired
    JiraCapability mockJiraService

    @Autowired
    RootAccountEntity testUser

    def "should not create organization with failed Jira connect"() {
        given:
        OrganizationInputDto organization = createOrganizationWithInvalidJira()

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
        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken())

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
        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken())

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail("janelle@dreamscale.io")

        JiraUserDto jiraUserDto = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build()
        mockJiraService.getUserByEmail(_, _) >> jiraUserDto

        when:

        MemberRegistrationDetailsDto membershipDto = organizationClient.registerMember(inviteOrg.getId().toString(), membershipInputDto)

        then:
        assert membershipDto != null
        assert membershipDto.getMemberId() != null
        assert membershipDto.getRootAccountId() != null
        assert membershipDto.getOrgEmail() == membershipInputDto.getOrgEmail()
        assert membershipDto.getFullName() == jiraUserDto.displayName
        assert membershipDto.getActivationCode() != null

    }


    def "should create a team within the org"() {
        given:
        OrganizationDto org = createOrganizationWithClient()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getRootAccountId())

        when:
        TeamDto team = teamClient.createTeam( "unicorn")

        then:
        assert team != null
        assert team.id != null
        assert team.name == "unicorn"
    }

    def "should get teams within the org"() {
        given:
        OrganizationDto org = createOrganizationWithClient()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getRootAccountId())


        TeamDto team1 = teamClient.createTeam("unicorn")
        TeamDto team2 = teamClient.createTeam("lightning")

        when:
        List<TeamDto> teams = teamClient.getAllTeams() //everyone team is here too

        then:
        assert teams != null
        assert teams.size() == 3
    }


    def "should add members to team"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getRootAccountId())


        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")

        TeamDto team = teamClient.createTeam("unicorn")

        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId, registration2.memberId])

        when:
        TeamMemberDto member1 = teamClient.addMemberToTeamWithMemberId("unicorn", registration1.memberId.toString())
        TeamMemberDto member2 = teamClient.addMemberToTeamWithMemberId("unicorn", registration2.memberId.toString())

        then:
        assert member1.memberId == registration1.memberId

        assert member2.memberId == registration2.memberId
    }


    def "should retrieve my teams that I am a member of"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        testUser.setId(member.getRootAccountId())


        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")

        TeamDto team1 = teamClient.createTeam("Team1")
        TeamDto team2 = teamClient.createTeam("Team2")
        TeamDto teamOther = teamClient.createTeam("other")

        teamClient.addMemberToTeamWithMemberId("Team1", registration1.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Team1", registration2.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Team2", registration1.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Team2", registration2.memberId.toString())

        teamClient.addMemberToTeamWithMemberId("other", registration2.memberId.toString())

        //active request coming from janelle
        testUser.id = registration1.rootAccountId

        when:
        List<TeamDto> myTeams = teamClient.getAllMyParticipatingTeams()

        then:
        assert myTeams != null //everyone here too
        assert myTeams.size() == 3

    }

    def "should retrieve team member status of specified team"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
        MemberRegistrationDetailsDto registration3 = registerMemberWithClient(org, "mike@dreamscale.io")

        testUser.setId(registration1.getRootAccountId())

        //login cant find home team, so this dont work, should be able to login without a team.
        accountClient.login()

        TeamDto team = teamClient.createTeam("Unicorn")

        println registration1.memberId
        println registration2.memberId
        println registration3.memberId

        teamClient.addMemberToTeamWithMemberId("Unicorn", registration1.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Unicorn", registration2.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Unicorn", registration3.memberId.toString())

        when:
        TeamWithMembersDto teamWithMembers = teamClient.getTeam("Unicorn")

        then:
        assert teamWithMembers.me.id == registration1.memberId
        assert teamWithMembers.teamMembers.size() == 2
    }

    def "should retrieve team member status of me and my team"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
        MemberRegistrationDetailsDto registration3 = registerMemberWithClient(org, "mike@dreamscale.io")

        testUser.setId(registration1.getRootAccountId())

        accountClient.login()

        TeamDto team = teamClient.createTeam("Unicorn")

        teamClient.addMemberToTeamWithMemberId("Unicorn", registration1.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Unicorn", registration2.memberId.toString())
        teamClient.addMemberToTeamWithMemberId("Unicorn", registration3.memberId.toString())

        when:
        TeamWithMembersDto meAndMyTeam = teamClient.getTeam("Unicorn")

        then:
        assert meAndMyTeam != null
        assert meAndMyTeam.getMe() != null
        assert meAndMyTeam.getTeamMembers().size() == 2
    }

    private MemberRegistrationDetailsDto registerMemberWithClient(OrganizationDto organizationDto, String memberEmail) {

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail(memberEmail)

        JiraUserDto jiraUserDto = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build()
        1 * mockJiraService.getUserByEmail(_, _) >> jiraUserDto

        return organizationClient.registerMember(organizationDto.getId().toString(), membershipInputDto)

    }

    private OrganizationDto createOrganizationWithClient() {
        OrganizationInputDto organization = createValidOrganization()

        return organizationClient.createOrganization(organization)
    }

    private OrganizationInputDto createValidOrganization() {
        OrganizationInputDto organization = new OrganizationInputDto()
        organization.setOrgName("DreamScale")
        organization.setDomainName("dreamscale.io")
        organization.setJiraUser("janelle@dreamscale.io")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("blah")

        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.VALID, null)

        return organization
    }

    private OrganizationInputDto createOrganizationWithInvalidJira() {
        OrganizationInputDto organization = new OrganizationInputDto()
        organization.setOrgName("DreamScale")
        organization.setJiraUser("fake")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("blabla")

        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.FAILED, "failed")

        return organization
    }

}

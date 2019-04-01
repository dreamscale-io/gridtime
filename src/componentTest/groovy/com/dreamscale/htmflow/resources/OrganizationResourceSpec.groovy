package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.organization.MembershipInputDto
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto
import com.dreamscale.htmflow.api.organization.TeamWithMembersDto
import com.dreamscale.htmflow.api.status.ConnectionResultDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.api.team.TeamDto
import com.dreamscale.htmflow.api.team.TeamInputDto
import com.dreamscale.htmflow.api.team.TeamMemberDto
import com.dreamscale.htmflow.api.team.TeamMembersToAddInputDto
import com.dreamscale.htmflow.client.AccountClient
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.MasterAccountRepository
import com.dreamscale.htmflow.core.domain.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.domain.TeamMemberRepository
import com.dreamscale.htmflow.core.domain.TeamRepository
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
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
	MasterAccountRepository masterAccountRepository

    @Autowired
    TeamRepository teamRepository

    @Autowired
    TeamMemberRepository teamMemberRepository

    @Autowired
    JiraService mockJiraService

    @Autowired
    MasterAccountEntity testUser

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
        assert membershipDto.getMasterAccountId() != null
        assert membershipDto.getOrgEmail() == membershipInputDto.getOrgEmail()
        assert membershipDto.getFullName() == jiraUserDto.displayName
        assert membershipDto.getActivationCode() != null

    }


    def "should create a team within the org"() {
        given:
        OrganizationDto org = createOrganizationWithClient()

        when:
        TeamDto team = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Unicorn"))

        then:
        assert team != null
        assert team.id != null
        assert team.name == "Team Unicorn"
    }

    def "should get teams within the org"() {
        given:
        OrganizationDto org = createOrganizationWithClient()

        TeamDto team1 = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Unicorn"))
        TeamDto team2 = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Lightning"))

        when:
        List<TeamDto> teams = organizationClient.getTeams(org.id.toString())

        then:
        assert teams != null
        assert teams.size() == 2
    }


    def "should add members to team"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")

        TeamDto team = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Unicorn"))

        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId, registration2.memberId])

        when:
        List<TeamMemberDto> teamMembers = organizationClient.addMembersToTeam(org.id.toString(), team.id.toString(), teamMembersToAdd)

        then:
        assert teamMembers != null
        assert teamMembers.size() == 2
        assert teamMembers.get(0).memberId == registration1.memberId
        assert teamMembers.get(0).memberName == registration1.fullName
        assert teamMembers.get(0).memberEmail == registration1.orgEmail
        assert teamMembers.get(0).teamName == team.name

        assert teamMembers.get(1).memberId == registration2.memberId
        assert teamMembers.get(1).memberName == registration2.fullName
        assert teamMembers.get(1).memberEmail == registration2.orgEmail

    }

    def "should add member to my team with email"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        TeamDto team = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Unicorn"))

        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId])

        List<TeamMemberDto> teamMembers = organizationClient.addMembersToTeam(org.id.toString(), team.id.toString(), teamMembersToAdd)

        JiraUserDto jiraUserDto = aRandom.jiraUserDto().emailAddress("kara@dreamscale.io").build()
        1 * mockJiraService.getUserByEmail(_, _) >> jiraUserDto

        when:
        testUser.id = registration1.masterAccountId;
        MemberRegistrationDetailsDto registration2 = organizationClient.addMemberToMyTeam("kara@dreamscale.io")

        then:
        assert registration2 != null
        assert registration2.orgEmail == "kara@dreamscale.io"
        assert registration2.activationCode != null

    }


    def "should retrieve my teams that I am a member of"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")

        TeamDto team1 = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team 1"))
        TeamDto team2 = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team 2"))
        TeamDto teamOther = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Other"))

        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId, registration2.memberId])

        organizationClient.addMembersToTeam(org.id.toString(), team1.id.toString(), teamMembersToAdd)
        organizationClient.addMembersToTeam(org.id.toString(), team2.id.toString(), teamMembersToAdd)

        TeamMembersToAddInputDto otherTeamMembers = new TeamMembersToAddInputDto([registration2.memberId])
        organizationClient.addMembersToTeam(org.id.toString(), teamOther.id.toString(), otherTeamMembers)

        //active request coming from janelle
        testUser.id = registration1.masterAccountId

        when:
        List<TeamDto> myTeams = organizationClient.getMyTeams(org.id.toString())

        then:
        assert myTeams != null
        assert myTeams.size() == 2

    }

    def "should retrieve team member status of specified team"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
        MemberRegistrationDetailsDto registration3 = registerMemberWithClient(org, "mike@dreamscale.io")

        TeamDto team = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Unicorn"))

        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId, registration2.memberId, registration3.memberId])

        organizationClient.addMembersToTeam(org.id.toString(), team.id.toString(), teamMembersToAdd)

        when:
        List<TeamMemberWorkStatusDto> teamMemberStatusList = organizationClient.getStatusOfTeamMembers(org.id.toString(), team.id.toString())

        then:
        assert teamMemberStatusList != null
        assert teamMemberStatusList.size() == 3
    }

    def "should retrieve team member status of me and my team"() {
        given:

        OrganizationDto org = createOrganizationWithClient()

        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
        MemberRegistrationDetailsDto registration3 = registerMemberWithClient(org, "mike@dreamscale.io")

        TeamDto team = organizationClient.createTeam(org.id.toString(), new TeamInputDto("Team Unicorn"))

        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId, registration2.memberId, registration3.memberId])

        organizationClient.addMembersToTeam(org.id.toString(), team.id.toString(), teamMembersToAdd)

        testUser.id = registration1.masterAccountId;

        when:
        TeamWithMembersDto meAndMyTeam = organizationClient.getMeAndMyTeam()

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

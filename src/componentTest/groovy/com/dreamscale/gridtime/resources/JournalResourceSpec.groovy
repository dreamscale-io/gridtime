package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.journal.*
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.CreateTaskInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.RecentTasksSummaryDto
import com.dreamscale.gridtime.api.project.TaskDto
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.capability.external.EmailCapability
import com.dreamscale.gridtime.core.domain.active.RecentProjectRepository
import com.dreamscale.gridtime.core.domain.active.RecentTaskRepository
import com.dreamscale.gridtime.core.domain.journal.IntentionRepository
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.journal.TaskRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.mapper.DateTimeAPITranslator
import com.dreamscale.gridtime.core.capability.system.GridClock
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class JournalResourceSpec extends Specification {

    @Autowired
    AccountClient accountClient

    @Autowired
    TeamClient teamClient

    @Autowired
    JournalClient journalClient

    @Autowired
    private LearningCircuitClient circleClient;

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    ProjectRepository projectRepository
    @Autowired
    TaskRepository taskRepository
    @Autowired
    IntentionRepository intentionRepository

    @Autowired
    RecentProjectRepository recentProjectRepository

    @Autowired
    RecentTaskRepository recentTaskRepository

    @Autowired
    RootAccountRepository rootAccountRepository;

    @Autowired
    GridClock mockGridClock

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    EmailCapability mockEmailCapability

    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4


    def setup() {
        time1 = LocalDateTime.now()
        time2 = time1.plusMinutes(15)
        time3 = time1.plusMinutes(30)
        time4 = time1.plusMinutes(45)
    }


    def "should save new intention"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        when:

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        JournalEntryDto journalEntry = journalClient.createIntention(new IntentionInputDto("My Intention", project.getId(), task.getId()))

        then:
        assert project != null
        assert task != null
        assert journalEntry != null

        assert journalEntry.getId() != null
        assert journalEntry.getTaskId() == task.getId()
        assert journalEntry.getTaskName() == task.getName()
        assert journalEntry.getTaskSummary() == task.getDescription()
        assert journalEntry.getProjectName() == project.getName()
        assert journalEntry.getDescription() == "My Intention"
        assert journalEntry.journalEntryType == JournalEntryType.Intention
        assert journalEntry.getMemberId() != null
        assert journalEntry.getUsername() != null
        assert journalEntry.getCreatedDate() != null

    }

    def "should update flame rating"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        int flameRating = 3;

        JournalEntryDto intention = journalClient.createIntention(new IntentionInputDto("My Intention", project.getId(), task.getId()))

        when:

        JournalEntryDto result = journalClient.updateFlameRating(intention.getId().toString(), new FlameRatingInputDto(flameRating));

        then:
        assert result != null
        assert result.getId() == intention.getId()
        assert result.getFlameRating() == flameRating;
    }


    def "should finish intention"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        JournalEntryDto intention = journalClient.createIntention(new IntentionInputDto("My Intention", project.getId(), task.getId()))

        when:
        JournalEntryDto result = journalClient.finishIntention(intention.getId().toString(), new IntentionFinishInputDto(FinishStatus.done));

        then:
        assert result != null
        assert result.getId() == intention.getId()
        assert result.getFinishStatus() == "done";
        assert result.getFinishTime() != null
        assert result.getFinishTimeStr() != null

    }


    def "should create new private task inside public project"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        when:

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", true))

        JournalEntryDto journalEntry = journalClient.createIntention(new IntentionInputDto("My Intention", project.getId(), task.getId()))

        RecentTasksSummaryDto recent = journalClient.getRecentProjectsAndTasks();

        RecentJournalDto journal = journalClient.getRecentJournal();

        then:
        assert project != null
        assert task != null
        assert journalEntry != null
        assert recent != null

        assert recent.getRecentTasks(project.getId()).size() == 2
        assert recent.getRecentTasks(project.getId()).get(0).isPrivate() == true

        assert journal.recentIntentions.size() == 2
        assert journal.recentProjects.size() == 2

    }

    def "should throw validation error when finishing an already finished intention"() {
        given:

        6 * mockGridClock.now() >> time1

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        1 * mockGridClock.now() >> time2

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("intention1", project.getId(), task.getId()))

        1 * mockGridClock.now() >> time3

        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("intention2", project.getId(), task.getId()))

        when:
        1 * mockGridClock.now() >> time4

        journalClient.finishIntention(intention1.getId().toString(), new IntentionFinishInputDto(FinishStatus.done));

        then:
        thrown (BadRequestException)
    }

    def "get task breakdown even when no new intentions"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        when:
        RecentJournalDto journal = journalClient.getRecentJournal()

        then:
        assert journal != null
        assert journal.getRecentProjects().size() == 1
        assert journal.getRecentTasksByProjectId().size() == 1

        assert journal.getRecentIntentions().size() == 1 //task switch event added, and welcome message
    }


    def "get recent intentions"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project.getId(), task.getId()))
        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("My Intention 2", project.getId(), task.getId()))

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournal().recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 3 //task switch event added, and welcome message
    }

    def "get recent intentions with limit"() {
        given:
        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project.getId(), task.getId()))
        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("My Intention 2", project.getId(), task.getId()))
        JournalEntryDto intention3 = journalClient.createIntention(new IntentionInputDto("My Intention 3", project.getId(), task.getId()))
        JournalEntryDto intention4 = journalClient.createIntention(new IntentionInputDto("My Intention 4", project.getId(), task.getId()))

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalWithLimit(3).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 3
    }

    def "get historical intentions before date"() {
        given:
        6 * mockGridClock.now() >> LocalDateTime.now().minusDays(5)

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        ConnectionStatusDto connection = accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("my-project", "proj description", false))
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        3 * mockGridClock.now() >> LocalDateTime.now().minusDays(5)
        3 * mockGridClock.nanoTime() >> System.nanoTime()

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project.getId(), task.getId()))
        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("My Intention 2", project.getId(), task.getId()))
        JournalEntryDto intention3 = journalClient.createIntention(new IntentionInputDto("My Intention 3", project.getId(), task.getId()))

        1 * mockGridClock.now() >> LocalDateTime.now()
        1 * mockGridClock.nanoTime() >> System.nanoTime()

        JournalEntryDto intention4 = journalClient.createIntention(new IntentionInputDto("My Intention 4", project.getId(), task.getId()))

        String beforeDateStr = DateTimeAPITranslator.convertToString(LocalDateTime.now().minusDays(1))

        when:
        List<JournalEntryDto> intentions = journalClient.getHistoricalIntentionsWithLimit(connection.getUsername(), beforeDateStr, 5)

        then:
        assert intentions != null
        assert intentions.size() == 4 //welcome message + 3
    }

    def "get recent tasks summary"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "proj1 description", false))
        ProjectDto project2 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj2", "proj2 description", false))

        TaskDto task1 = journalClient.findOrCreateTask(project1.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))
        TaskDto task2 = journalClient.findOrCreateTask(project1.getId().toString(), new CreateTaskInputDto("DS-112", "my task description", false))
        TaskDto task3 = journalClient.findOrCreateTask(project2.getId().toString(), new CreateTaskInputDto("DS-113", "my task description", false))
        TaskDto task4 = journalClient.findOrCreateTask(project2.getId().toString(), new CreateTaskInputDto("DS-114", "my task description", false))

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project1.getId(), task1.getId()))
        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("My Intention 2", project1.getId(), task2.getId()))
        JournalEntryDto intention3 = journalClient.createIntention(new IntentionInputDto("My Intention 3", project2.getId(), task3.getId()))
        JournalEntryDto intention4 = journalClient.createIntention(new IntentionInputDto("My Intention 4", project2.getId(), task4.getId()))

        when:

        RecentTasksSummaryDto recentTasksSummary = journalClient.getRecentProjectsAndTasks();

        then:
        assert recentTasksSummary != null
        assert recentTasksSummary.getRecentProjects().size() == 3 //no project project

        ProjectDto recentProject1 = recentTasksSummary.getRecentProjects().get(1)
        ProjectDto recentProject2 = recentTasksSummary.getRecentProjects().get(2)

        assert recentTasksSummary.getRecentTasks(recentProject1.getId()).size() == 3
        assert recentTasksSummary.getRecentTasks(recentProject2.getId()).size() == 3

    }

    def "create a new task reference in the journal"() {
        given:
        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "proj1 description", false))
        ProjectDto project2 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj2", "proj2 description", false))

        TaskDto task1 = journalClient.findOrCreateTask(project1.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))
        TaskDto task2 = journalClient.findOrCreateTask(project1.getId().toString(), new CreateTaskInputDto("DS-112", "my task description", false))

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project1.getId(), task1.getId()))

        TaskReferenceInputDto taskReferenceDto = new TaskReferenceInputDto()
        taskReferenceDto.setTaskName(task1.name)

        when:

        RecentTasksSummaryDto recentTasksSummary = journalClient.createTaskReference(taskReferenceDto);

        then:
        assert recentTasksSummary.getActiveTask() != null

        assert recentTasksSummary != null
        assert recentTasksSummary.getRecentProjects().size() == 3

        assert recentTasksSummary.getRecentTasks(project1.getId()).size() == 3;
        assert recentTasksSummary.getRecentTasks(project2.getId()).size() == 1;

    }

    def "get recent intentions for other member"() {
        given:
        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");
        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "proj1 description", false))

        TaskDto task1 = journalClient.findOrCreateTask(project1.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project1.getId(), task1.getId()))
        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("My Intention 2", project1.getId(), task1.getId()))

        //change active logged in user to a different user within same organization

        AccountActivationDto zoeProfile = registerAndActivate("zoe@dreamscale.io");
        switchUser(zoeProfile)

        teamClient.joinTeam("myteam")

        accountClient.login()

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalForUser(artyProfile.getUsername()).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 3 //welcome message + 2
    }

    def "get recent intentions for other member with limit"() {
        given:
        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");
        switchUser(artyProfile)

        accountClient.login()

        TeamDto team = teamClient.createTeam("myteam")

        ProjectDto project1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "proj1 description", false))

        TaskDto task1 = journalClient.findOrCreateTask(project1.getId().toString(), new CreateTaskInputDto("DS-111", "my task description", false))

        JournalEntryDto intention1 = journalClient.createIntention(new IntentionInputDto("My Intention 1", project1.getId(), task1.getId()))
        JournalEntryDto intention2 = journalClient.createIntention(new IntentionInputDto("My Intention 2", project1.getId(), task1.getId()))

        //change active logged in user to a different user within same organization

        AccountActivationDto zoeProfile = registerAndActivate("zoe@dreamscale.io");
        switchUser(zoeProfile)

        teamClient.joinTeam("myteam")

        accountClient.login()

        when:
        List<JournalEntryDto> intentions = journalClient.getRecentJournalForUserWithLimit(
                artyProfile.getUsername(), 1).recentIntentions

        then:
        assert intentions != null
        assert intentions.size() == 1
    }

    private JournalEntryDto createIntentionWithClient(IntentionInputDto intentionInputDto) {
        return journalClient.createIntention(intentionInputDto)
    }

    private TaskEntity createOrganizationAndTask() {
        OrganizationEntity organization = aRandom.organizationEntity().save()

        ProjectEntity project = aRandom.projectEntity().forOrg(organization).save()

        TaskEntity task = aRandom.taskEntity().forProject(project).save()

        return task
    }

    private OrganizationMemberEntity createMembership(UUID organizationId, UUID rootAccountId, UUID teamId) {

        OrganizationMemberEntity member = aRandom.memberEntity()
                .organizationId(organizationId)
                .rootAccountId(rootAccountId)
                .save()

        TeamMemberEntity teamMember = aRandom.teamMemberEntity()
                .organizationId(organizationId)
                .memberId(member.getId())
                .teamId(teamId)
                .save()



        return member
    }

    private AccountActivationDto registerAndActivate(String email) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)

        String activationToken = null;

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { emailAddr, token -> activationToken = token; return null}

        UserProfileDto userProfileDto = accountClient.register(rootAccountInput)
        return accountClient.activate(new ActivationCodeDto(activationToken))
    }

    private void switchUser(AccountActivationDto artyProfile) {
        RootAccountEntity account = rootAccountRepository.findByApiKey(artyProfile.getApiKey());

        loggedInUser.setId(account.getId())
        loggedInUser.setApiKey(account.getApiKey())
    }

}

package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.CreateTaskInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.TaskDto
import com.dreamscale.gridtime.api.project.TaskInputDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.client.ProjectClient
import com.dreamscale.gridtime.core.capability.external.EmailCapability
import com.dreamscale.gridtime.core.capability.system.GridClock
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository
import com.dreamscale.gridtime.core.domain.journal.TaskRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraTaskDto
import com.dreamscale.gridtime.core.capability.external.JiraCapability
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class ProjectResourceSpec extends Specification {

    @Autowired
    AccountClient accountClient

    @Autowired
    ProjectClient projectClient

    @Autowired
    JournalClient journalClient

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    ProjectRepository projectRepository
    @Autowired
    TaskRepository taskRepository

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    JiraCapability mockJiraService

    @Autowired
    RootAccountRepository rootAccountRepository;

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    EmailCapability mockEmailCapability

    @Autowired
    GridClock mockGridClock

    def "should retrieve project list"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.createProject(new CreateProjectInputDto("proj1", "desc", true))
        ProjectDto proj2 = journalClient.createProject(new CreateProjectInputDto("proj2", "desc", true))

        when:
        List<ProjectDto> projects = projectClient.getProjects()

        then:
        assert projects.size() == 3
        assert projects[1].id == proj1.id
        assert projects[1].name == proj1.name
        assert projects[1].externalId == proj1.externalId
    }

    def "should find tasks starting with search string"() {
        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.createProject(new CreateProjectInputDto("proj1", "desc", true))

        TaskDto task1 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-123", "desc"))
        TaskDto task2 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-124", "desc"))
        TaskDto task3 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-137", "desc"))
        TaskDto task4 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-211", "desc"))

        when:
        List<TaskDto> tasks = projectClient.findTasksStartingWith(proj1.id.toString(), "FD-1")

        then:
        assert tasks.size() == 3
    }

    def "should refuse to search when search string is too short"() {
        given:
        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = registerAndActivate("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.createProject(new CreateProjectInputDto("proj1", "desc", true))

        TaskDto task1 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-123", "desc"))
        TaskDto task2 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-124", "desc"))
        TaskDto task3 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-137", "desc"))
        TaskDto task4 = journalClient.createTask(proj1.getId().toString(), new CreateTaskInputDto("FD-211", "desc"))

        when:
        projectClient.findTasksStartingWith(proj1.id.toString(), "FD-")

        then:
        thrown(BadRequestException)
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

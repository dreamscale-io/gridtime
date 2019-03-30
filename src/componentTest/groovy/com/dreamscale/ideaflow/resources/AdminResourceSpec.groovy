package com.dreamscale.ideaflow.resources

import com.dreamscale.ideaflow.ComponentTest
import com.dreamscale.ideaflow.api.admin.ProjectSyncInputDto
import com.dreamscale.ideaflow.api.admin.ProjectSyncOutputDto
import com.dreamscale.ideaflow.client.AdminClient
import com.dreamscale.ideaflow.core.domain.ConfigProjectSyncRepository
import com.dreamscale.ideaflow.core.domain.OrganizationEntity
import com.dreamscale.ideaflow.core.domain.OrganizationRepository
import com.dreamscale.ideaflow.core.domain.ProjectEntity
import com.dreamscale.ideaflow.core.domain.ProjectRepository
import com.dreamscale.ideaflow.core.domain.TaskEntity
import com.dreamscale.ideaflow.core.domain.TaskRepository
import com.dreamscale.ideaflow.core.hooks.jira.dto.JiraProjectDto
import com.dreamscale.ideaflow.core.hooks.jira.dto.JiraTaskDto
import com.dreamscale.ideaflow.core.service.JiraService
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

@ComponentTest
public class AdminResourceSpec extends Specification {

    @Autowired
    AdminClient adminClient

    @Autowired
    JiraService mockJiraService

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    ConfigProjectSyncRepository configProjectSyncRepository

    @Autowired
    ProjectRepository projectRepository

    @Autowired
    TaskRepository taskRepository

    def "should configure project sync for the org"() {

        given:
        OrganizationEntity organizationEntity = aRandom.organizationEntity().save()

        ProjectSyncInputDto syncDto = new ProjectSyncInputDto(organizationEntity.getId(), "jira_project")

        JiraProjectDto jiraProjectDto = aRandom.jiraProjectDto().name("jira_project").build()

        mockJiraService.getProjectByName(organizationEntity.id, "jira_project") >> jiraProjectDto

        when:
        ProjectSyncOutputDto outputDto = adminClient.configProjectSync(syncDto)

        then:
        assert outputDto.id != null

    }

    def "should throw an error if the org does not exist"() {
        given:
        ProjectSyncInputDto syncDto = new ProjectSyncInputDto(UUID.randomUUID(), "jira_project")

        when:
        ProjectSyncOutputDto outputDto = adminClient.configProjectSync(syncDto)

        then:
        thrown(BadRequestException)
    }

    def "should throw an error if jira project is not valid"() {

        given:
        OrganizationEntity organizationEntity = aRandom.organizationEntity().save()

        ProjectSyncInputDto syncDto = new ProjectSyncInputDto(organizationEntity.getId(), "invalid_project")

        when:
        ProjectSyncOutputDto outputDto = adminClient.configProjectSync(syncDto)

        then:
        thrown(BadRequestException)

    }

    def "should sync configured projects and tasks"() {
        given:
        OrganizationEntity organizationEntity = aRandom.organizationEntity().save()

        JiraProjectDto jiraProjectDto = aRandom.jiraProjectDto().name("jira_project").build()

        JiraTaskDto jiraTaskDto = aRandom.jiraTaskDto().build()

        mockJiraService.getProjectByName(organizationEntity.id, "jira_project") >> jiraProjectDto
        mockJiraService.getFilteredProjects(organizationEntity.id, _) >> [jiraProjectDto]
        mockJiraService.getOpenTasksForProject(organizationEntity.id, jiraProjectDto.id) >> [jiraTaskDto]

        when:
        adminClient.syncAllOrgs()

        then:

        List<ProjectEntity> dbProjects = projectRepository.findByOrganizationId(organizationEntity.id)
        assert dbProjects != null
        assert dbProjects.size() > 0

        List< TaskEntity> dbTasks = taskRepository.findByProjectId(dbProjects.get(0).id)
        assert dbTasks.size() > 0

    }

}
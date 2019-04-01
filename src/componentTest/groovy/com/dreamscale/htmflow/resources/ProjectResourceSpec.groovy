package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.TaskDto
import com.dreamscale.htmflow.api.project.TaskInputDto
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.client.ProjectClient
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.OrganizationEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.domain.ProjectEntity
import com.dreamscale.htmflow.core.domain.ProjectRepository
import com.dreamscale.htmflow.core.domain.TaskRepository
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTaskDto
import com.dreamscale.htmflow.core.service.JiraService
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class ProjectResourceSpec extends Specification {

    @Autowired
    ProjectClient projectClient

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
    JiraService mockJiraService

    @Autowired
    MasterAccountEntity testUser

    def "should retrieve project list"() {
        given:
        OrganizationEntity org = createOrgAndTestUserMembership()

        ProjectEntity projectEntity = aRandom.projectEntity().forOrg(org).save()

        when:
        List<ProjectDto> projects = projectClient.getProjects()

        then:
        assert projects.size() == 1
        assert projects[0].id == projectEntity.id
        assert projects[0].name == projectEntity.name
        assert projects[0].externalId == projectEntity.externalId
    }

    def "should find tasks starting with search string"() {
        given:
        OrganizationEntity organizationEntity = createOrgAndTestUserMembership()

        ProjectEntity projectEntity = aRandom.projectEntity().forOrg(organizationEntity).save()

        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-123").save()
        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-124").save()
        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-137").save()
        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-211").save()

        when:
        List<TaskDto> tasks = projectClient.findTasksStartingWith(projectEntity.id.toString(), "FD-1")

        then:
        assert tasks.size() == 3
    }

    def "should refuse to search when search string is too short"() {
        given:
        OrganizationEntity organizationEntity = createOrgAndTestUserMembership()

        ProjectEntity projectEntity = aRandom.projectEntity().forOrg(organizationEntity).save()

        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-123").save()
        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-124").save()
        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-227").save()
        aRandom.taskEntity().forProjectAndName(projectEntity, "FD-211").save()

        when:
        projectClient.findTasksStartingWith(projectEntity.id.toString(), "FD-")

        then:
        thrown(BadRequestException)
    }

    def "should create new task"() {
        given:

        OrganizationEntity org = createOrgAndTestUserMembership()

        ProjectEntity projectEntity = aRandom.projectEntity().forOrg(org).save()

        JiraTaskDto newJiraTaskDto = aRandom.jiraTaskDto().build()
        mockJiraService.createNewTask(_, _, _, _) >> newJiraTaskDto

        TaskInputDto taskInputDto = new TaskInputDto(newJiraTaskDto.summary, "description!!")

        when:
        TaskDto task = projectClient.createNewTask(projectEntity.getId().toString(), taskInputDto)

        then:
        assert task != null
        assert task.externalId != null
        assert task.summary == newJiraTaskDto.summary
    }

    private OrganizationEntity createOrgAndTestUserMembership() {
        OrganizationEntity organization = aRandom.organizationEntity().save()
        aRandom.memberEntity()
                .forOrgAndAccount(organization, testUser)
                .save()
        return organization
    }

}

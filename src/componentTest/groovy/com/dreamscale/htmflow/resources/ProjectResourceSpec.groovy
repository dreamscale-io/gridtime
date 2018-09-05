package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.TaskDto
import com.dreamscale.htmflow.api.project.TaskInputDto
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.client.ProjectClient
import com.dreamscale.htmflow.core.domain.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.OrganizationEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.domain.ProjectEntity
import com.dreamscale.htmflow.core.domain.ProjectRepository
import com.dreamscale.htmflow.core.domain.TaskEntity
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

	def setup() {
		projectRepository.deleteAll()
		taskRepository.deleteAll()
		organizationRepository.deleteAll()
		organizationMemberRepository.deleteAll()
	}

	def "should retrieve project list"() {
		given:
		OrganizationEntity org = createOrgAndTestUserMembership();

		ProjectEntity projectEntity = aRandom.projectEntity().organizationId(org.id).build()
		projectRepository.save(projectEntity)

		when:
		List<ProjectDto> projects = projectClient.getProjects()

		then:
		assert projects.size() == 1
		assert projects[0].id == projectEntity.id
		assert projects[0].name == projectEntity.name
		assert projects[0].externalId == projectEntity.externalId
	}

	def "should retrieve task list"() {
		given:
		OrganizationEntity org = createOrgAndTestUserMembership();

		ProjectEntity projectEntity = aRandom.projectEntity().organizationId(org.id).build()
		projectRepository.save(projectEntity)

		TaskEntity taskEntity1 = aRandom.taskEntity().projectId(projectEntity.id).build()
		TaskEntity taskEntity2 = aRandom.taskEntity().projectId(projectEntity.id).build()
		TaskEntity taskEntity3 = aRandom.taskEntity().projectId(projectEntity.id).build()
		TaskEntity taskEntity4 = aRandom.taskEntity().projectId(projectEntity.id).build()

		taskRepository.save(taskEntity1)
		taskRepository.save(taskEntity2)
		taskRepository.save(taskEntity3)
		taskRepository.save(taskEntity4)

		when:
		List<TaskDto> tasks = projectClient.getOpenTasksForProject(projectEntity.id.toString())

		then:
		assert tasks.size() == 4
	}

	def "should find tasks starting with search string"() {
		given:
		OrganizationEntity organizationEntity = createOrgAndTestUserMembership();

		ProjectEntity projectEntity = aRandom.projectEntity().organizationId(organizationEntity.getId()).build()
		projectRepository.save(projectEntity)

		TaskEntity taskEntity1 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-123").build()
		TaskEntity taskEntity2 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-124").build()
		TaskEntity taskEntity3 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-137").build()
		TaskEntity taskEntity4 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-211").build()

		taskRepository.save(taskEntity1)
		taskRepository.save(taskEntity2)
		taskRepository.save(taskEntity3)
		taskRepository.save(taskEntity4)

		when:
		List<TaskDto> tasks = projectClient.findTasksStartingWith(projectEntity.id.toString(), "FD-1")

		then:
		assert tasks.size() == 3
	}

	def "should refuse to search when search string is too short"() {
		given:
		OrganizationEntity organizationEntity = createOrgAndTestUserMembership();

		ProjectEntity projectEntity = aRandom.projectEntity().organizationId(organizationEntity.getId()).build()
		projectRepository.save(projectEntity)

		TaskEntity taskEntity1 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-123").build()
		TaskEntity taskEntity2 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-124").build()
		TaskEntity taskEntity3 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-227").build()
		TaskEntity taskEntity4 = aRandom.taskEntity().projectId(projectEntity.id).name("FD-211").build()

		taskRepository.save(taskEntity1)
		taskRepository.save(taskEntity2)
		taskRepository.save(taskEntity3)
		taskRepository.save(taskEntity4)

		when:
		List<TaskDto> tasks = projectClient.findTasksStartingWith(projectEntity.id.toString(), "FD-")

		then:
		thrown(BadRequestException)
	}

	def "should create new task"() {
		given:

		OrganizationEntity org = createOrgAndTestUserMembership();

		ProjectEntity projectEntity = aRandom.projectEntity().organizationId(org.id).build()
		projectRepository.save(projectEntity)

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
		OrganizationEntity organizationEntity = createOrganization();
		organizationRepository.save(organizationEntity)

		OrganizationMemberEntity organizationMemberEntity = createOrganizationMember(organizationEntity.getId(), testUser.getId())
		organizationMemberRepository.save(organizationMemberEntity)

		return organizationEntity;

	}

	private OrganizationEntity createOrganization() {
		return OrganizationEntity.builder()
				.id(UUID.randomUUID())
				.orgName("DreamScale")
				.jiraUser("janelle@dreamscale.io")
				.jiraSiteUrl("dreamscale.atlassian.net")
				.jiraApiKey("9KC0iM24tfXf8iKDVP2q4198")
				.build()
	}

	private OrganizationMemberEntity createOrganizationMember(UUID organizationId, UUID masterAccountId) {
		return OrganizationMemberEntity.builder()
				.id(UUID.randomUUID())
				.masterAccountId(masterAccountId)
				.organizationId(organizationId)
				.build()
	}






}

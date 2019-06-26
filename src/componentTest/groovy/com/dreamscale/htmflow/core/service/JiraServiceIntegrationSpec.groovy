package com.dreamscale.htmflow.core.service

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.project.TaskInputDto
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationRepository
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraSearchResultPage
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTaskDto
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
public class JiraServiceIntegrationSpec extends Specification {

	JiraService jiraService

	@Autowired
	OrganizationClient organizationClient

	@Autowired
	OrganizationRepository organizationRepository

	@Autowired
	JiraConnectionFactory jiraConnectionFactory


	def setup() {
		jiraService = new JiraService()
		jiraService.organizationRepository = organizationRepository
		jiraService.jiraConnectionFactory = jiraConnectionFactory
	}

	def "should fetch a jira project by name"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		when:
		JiraProjectDto jiraProjectDto = jiraService.getProjectByName(validOrg.getId(), "dummy-test")

		then:
		assert jiraProjectDto != null
	}

	def "should fetch a jira ticket by id"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		when:
		JiraTaskDto jiraTaskDto = jiraService.getTask(validOrg.getId(), "FP-207")

		then:
		assert jiraTaskDto != null
		assert jiraTaskDto.key == "FP-207"
	}


	def "should fetch a user by email"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		when:
		JiraUserDto jiraUserDto = jiraService.getUserByEmail(validOrg.getId(), "janelle@dreamscale.io")

		then:
		assert jiraUserDto != null
	}

	def "should throw exception if org not found"() {
		given:
		when:
		JiraProjectDto jiraProjectDto = jiraService.getProjectByName(UUID.randomUUID(), "dummy-test")

		then:
		thrown(BadRequestException)
	}

	def "should throw exception if project not found"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		when:
		JiraProjectDto jiraProjectDto = jiraService.getProjectByName(validOrg.id, "unknown-project")

		then:
		thrown(BadRequestException)
	}

	def "should filter projects by list of ids"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		JiraProjectDto project1 = jiraService.getProjectByName(validOrg.getId(), "flow-data-plugins")
		JiraProjectDto project2 = jiraService.getProjectByName(validOrg.getId(), "dummy-test")


		when:
		List<JiraProjectDto> filteredProjects = jiraService.getFilteredProjects(validOrg.id, [project1.id, project2.id])

		then:
		assert filteredProjects.size() == 2
	}

	def "should get all open tasks for project"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		JiraProjectDto project = jiraService.getProjectByName(validOrg.getId(), "dummy-test")

		when:
		List<JiraTaskDto> openTasks = jiraService.getOpenTasksForProject(validOrg.id, project.id)

		then:
		assert openTasks.size() == 6
	}

	def "should page through all tasks for project"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		JiraProjectDto project = jiraService.getProjectByName(validOrg.getId(), "dummy-test")

		when:
		JiraSearchResultPage page1 = jiraService.getOpenTasksForProject(validOrg.id, project.id, 0, 2)
		JiraSearchResultPage page2 = jiraService.getOpenTasksForProject(validOrg.id, project.id, 2, 2)

		then:
		assert page1.issues.size() == 2
		assert page2.issues.size() == 2

	}

	def "should create new task and move to in progress and assign then done"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)
		
		JiraProjectDto project = jiraService.getProjectByName(validOrg.getId(), "dummy-test")
		JiraUserDto jiraUser = jiraService.getUserByEmail(validOrg.getId(), "janelle@dreamscale.io")

		TaskInputDto taskInputDto = new TaskInputDto("Hello summary!", "description!!")

		when:
		JiraTaskDto newTask = jiraService.createNewTask(validOrg.id, project.id, jiraUser.key, taskInputDto)

		then:
		assert newTask.key != null
		assert newTask.status == "In Progress"
		assert newTask.assignee == "janelle@dreamscale.io"

		when:
		JiraTaskDto closedTask = jiraService.closeTask(validOrg.id, newTask.key)

		then:
		assert closedTask != null
		assert closedTask.status == "Done"

		when:
		jiraService.deleteTask(validOrg.id, newTask.key)

		then:
		assert true

	}

	private OrganizationEntity createValidOrganization() {
		return OrganizationEntity.builder()
				.id(UUID.randomUUID())
				.orgName("DreamScale")
				.domainName("dreamscale.io")
				.jiraUser("janelle@dreamscale.io")
				.jiraSiteUrl("dreamscale.atlassian.net")
				.jiraApiKey("9KC0iM24tfXf8iKDVP2q4198")
				.build()
	}


}

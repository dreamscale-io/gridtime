package com.dreamscale.gridtime.core.service

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.project.TaskInputDto
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.hooks.jira.JiraConnectionFactory
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraProjectDto
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraSearchResultPage
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraTaskDto
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraUserDto
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@ComponentTest
public class JiraCapabilitySpec extends Specification {

	JiraCapability jiraService

	@Autowired
	OrganizationClient organizationClient

	@Autowired
	OrganizationRepository organizationRepository

	@Autowired
	JiraConnectionFactory jiraConnectionFactory


	def setup() {
		jiraService = new JiraCapability()
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


	def "should get all users"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		when:
		List<JiraUserDto> jiraUsers = jiraService.getUsers(validOrg.getId())

		then:
		assert jiraUsers != null
		assert jiraUsers.size() > 5
	}

	//TODO these JIRA features no longer work because of GDPR
	@Ignore
	def "should fetch a user by email"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)

		when:
		JiraUserDto jiraUserDto = jiraService.getUserByEmail(validOrg.getId(), "arty@dreamscale.io")

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
		assert openTasks.size() == 12
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

	@Ignore
	def "should create new task and move to in progress and assign then done"() {
		given:
		OrganizationEntity validOrg = createValidOrganization()
		organizationRepository.save(validOrg)
		
		JiraProjectDto project = jiraService.getProjectByName(validOrg.getId(), "dummy-test")
		JiraUserDto jiraUser = jiraService.getUserByEmail(validOrg.getId(), "zoe@dreamscale.io")

		TaskInputDto taskInputDto = new TaskInputDto("Hello summary!", "description!!")

		when:
		JiraTaskDto newTask = jiraService.createNewTask(validOrg.id, project.id, jiraUser.accountId, taskInputDto)

		then:
		assert newTask.key != null
		assert newTask.status == "In Progress"
		assert newTask.assignee == "zoe"

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
				.jiraUser("arty@dreamscale.io")
				.jiraSiteUrl("dreamscale.atlassian.net")
				.jiraApiKey("9KC0iM24tfXf8iKDVP2q4198")
				.build()
	}


}

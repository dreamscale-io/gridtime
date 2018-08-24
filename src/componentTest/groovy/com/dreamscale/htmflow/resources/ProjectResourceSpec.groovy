package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.organization.MembershipDetailsDto
import com.dreamscale.htmflow.api.organization.MembershipInputDto
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
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
import com.dreamscale.htmflow.core.domain.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
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
	MasterAccountEntity testUser;

	def setup() {
		projectRepository.deleteAll()
		taskRepository.deleteAll()
		organizationRepository.deleteAll()
		organizationMemberRepository.deleteAll()
	}

	def "should retrieve project list"() {
		given:
		OrganizationEntity organizationEntity = createOrganization();
		organizationRepository.save(organizationEntity)

		OrganizationMemberEntity organizationMemberEntity = createOrganizationMember(organizationEntity.getId(), testUser.getId())
		organizationMemberRepository.save(organizationMemberEntity)

		ProjectEntity entity = aRandom.projectEntity().build()
		projectRepository.save(entity)

		when:
		List<ProjectDto> projects = projectClient.getProjects()

		then:
		assert projects.size() == 6
		assert projects[0].id == entity.id
		assert projects[0].name == entity.name
		assert projects[0].externalId == entity.externalId
	}

	def "should retrieve task list"() {
		given:
		OrganizationEntity organizationEntity = createOrganization();
		organizationRepository.save(organizationEntity)

		OrganizationMemberEntity organizationMemberEntity = createOrganizationMember(organizationEntity.getId(), testUser.getId())
		organizationMemberRepository.save(organizationMemberEntity)

		List<ProjectDto> projects = projectClient.getProjects();
		UUID projectId = projects.get(0).getId();

		when:
		List<TaskDto> tasks = projectClient.getOpenTasksForProject(projectId.toString())

		then:
		assert tasks.size() > 0
	}

	@Ignore
	def "should create new task"() {
		given:

		OrganizationInputDto organization = createOrganizationInput()

		OrganizationDto organizationDto = organizationClient.createOrganization(organization)
		OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken());

		MembershipInputDto membershipInputDto = new MembershipInputDto()
		membershipInputDto.setInviteToken(organizationDto.getInviteToken())
		membershipInputDto.setOrgEmail("janelle@dreamscale.io")

		MembershipDetailsDto memberDetails = organizationClient.registerMember(inviteOrg.getId().toString(), membershipInputDto)
		testUser.id = memberDetails.masterAccountId

		List<ProjectDto> projects = projectClient.getProjects();
		UUID projectId = projects.get(0).getId();

		TaskInputDto taskInputDto = new TaskInputDto("Hello summary!", "description!!")

		when:
		TaskDto task = projectClient.createNewTask(projectId.toString(), taskInputDto)

		then:
		println task
		assert task != null
		assert task.externalId != null
		assert task.summary == "Hello summary!"
	}

	private OrganizationInputDto createOrganizationInput() {
		OrganizationInputDto organization = new OrganizationInputDto();
		organization.setOrgName("DreamScale")
		organization.setDomainName("dreamscale.io")
		organization.setJiraUser("janelle@dreamscale.io")
		organization.setJiraSiteUrl("dreamscale.atlassian.net")
		organization.setJiraApiKey("9KC0iM24tfXf8iKDVP2q4198")

		return organization;
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

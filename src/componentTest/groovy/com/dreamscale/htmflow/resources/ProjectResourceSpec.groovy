package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.TaskDto
import com.dreamscale.htmflow.client.JournalClient
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
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class ProjectResourceSpec extends Specification {

	@Autowired
	ProjectClient projectClient
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
		assert projects.size() == 2
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

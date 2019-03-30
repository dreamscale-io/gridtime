package com.dreamscale.ideaflow.core.service

import com.dreamscale.ideaflow.ComponentTest
import com.dreamscale.ideaflow.core.domain.ConfigProjectSyncRepository
import com.dreamscale.ideaflow.core.domain.OrganizationEntity
import com.dreamscale.ideaflow.core.domain.OrganizationRepository
import com.dreamscale.ideaflow.core.domain.ProjectEntity
import com.dreamscale.ideaflow.core.domain.ProjectRepository
import com.dreamscale.ideaflow.core.domain.TaskEntity
import com.dreamscale.ideaflow.core.domain.TaskRepository
import com.dreamscale.ideaflow.core.hooks.jira.dto.JiraProjectDto
import com.dreamscale.ideaflow.core.hooks.jira.dto.JiraTaskDto
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

@ComponentTest
public class JiraSyncServiceSpec extends Specification {

	@Autowired
	OrganizationRepository organizationRepository

	@Autowired
	ConfigProjectSyncRepository configProjectSyncRepository

	@Autowired
	ProjectRepository projectRepository

	@Autowired
	TaskRepository taskRepository

	@Autowired
	JiraSyncService jiraSyncService

	@Autowired
	JiraService mockJiraService

	def "should update task status if updated in Jira"() {
		given:
		OrganizationEntity org = aRandom.organizationEntity().save()

		JiraProjectDto jiraProject = aRandom.jiraProjectDto().build()
		mockJiraService.getFilteredProjects(_, _) >> [jiraProject]

		JiraTaskDto jiraTask = aRandom.jiraTaskDto().status("In Progress").build()
		mockJiraService.getOpenTasksForProject(_, _) >> [jiraTask]

		when:
		jiraSyncService.synchronizeProjectsWithJira(org.id)

		then:
		List<ProjectEntity> projects = projectRepository.findByOrganizationId(org.id)
		assert projects.size() == 1

		List<TaskEntity> tasks = taskRepository.findByProjectId(projects.get(0).id)
		assert tasks.size() == 2

		TaskEntity nonDefaultTask = getNonDefaultTask(tasks);
		assert nonDefaultTask.status == "In Progress"

		when:
		jiraTask.fields.get("status").put("name", "Needs Review")
		jiraSyncService.synchronizeProjectsWithJira(org.id)

		then:
		List<TaskEntity> updatedTasks = taskRepository.findByProjectId(projects.get(0).id)
		assert updatedTasks.size() == 2

		TaskEntity reviewTask = getNonDefaultTask(updatedTasks);
		assert reviewTask.status == "Needs Review"

	}

	private TaskEntity getNonDefaultTask(List<TaskEntity> tasks) {
		TaskEntity nonDefaultTask = null;

		for (TaskEntity task : tasks) {
			if (!task.isDefaultTask()) {
				nonDefaultTask = task;
				break;
			}
		}
		return nonDefaultTask;
	}

	def "should close tasks if no longer retrieved from Jira"() {
		given:
		OrganizationEntity org = aRandom.organizationEntity().save()

		JiraProjectDto jiraProject = aRandom.jiraProjectDto().build()
		mockJiraService.getFilteredProjects(_, _) >> [jiraProject]

		JiraTaskDto jiraTask = aRandom.jiraTaskDto().status("In Progress").build()

		1 * mockJiraService.getOpenTasksForProject(_, _) >> [jiraTask]
		1 * mockJiraService.getOpenTasksForProject(_, _) >> []

		when:
		jiraSyncService.synchronizeProjectsWithJira(org.id)
		jiraSyncService.synchronizeProjectsWithJira(org.id)

		then:
		List<ProjectEntity> projects = projectRepository.findByOrganizationId(org.id)
		assert projects.size() == 1

		List<TaskEntity> tasks = taskRepository.findByProjectId(projects.get(0).id)
		assert tasks.size() == 2

		TaskEntity nonDefaultTask = getNonDefaultTask(tasks);
		assert nonDefaultTask.status == "Done"

	}

	def "should not explode if closed tasks are re-opened in Jira"() {
		given:
		OrganizationEntity org = aRandom.organizationEntity().save()

		JiraProjectDto jiraProject = aRandom.jiraProjectDto().build()
		mockJiraService.getFilteredProjects(_, _) >> [jiraProject]

		JiraTaskDto jiraTask = aRandom.jiraTaskDto().status("In Progress").build()

		1 * mockJiraService.getOpenTasksForProject(_, _) >> [jiraTask]
		1 * mockJiraService.getOpenTasksForProject(_, _) >> []
		1 * mockJiraService.getOpenTasksForProject(_, _) >> [jiraTask]

		when:
		jiraSyncService.synchronizeProjectsWithJira(org.id)
		jiraSyncService.synchronizeProjectsWithJira(org.id)
		jiraSyncService.synchronizeProjectsWithJira(org.id)

		then:
		List<ProjectEntity> projects = projectRepository.findByOrganizationId(org.id)
		assert projects.size() == 1

		List<TaskEntity> tasks = taskRepository.findByProjectId(projects.get(0).id)
		assert tasks.size() == 2

		TaskEntity nonDefaultTask = getNonDefaultTask(tasks);
		assert nonDefaultTask.status == "In Progress"
	}

}

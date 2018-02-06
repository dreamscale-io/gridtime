package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto
import com.dreamscale.htmflow.api.project.ProjectDto
import com.dreamscale.htmflow.api.project.TaskDto
import com.dreamscale.htmflow.client.JournalClient
import com.dreamscale.htmflow.client.ProjectClient
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

    def setup() {
        projectRepository.deleteAll()
        taskRepository.deleteAll()
    }

    def "should retrieve project list"() {
        given:
        ProjectEntity entity = aRandom.projectEntity().build()
        projectRepository.save(entity)

        when:
        List<ProjectDto> projects = projectClient.getProjects()

        then:
        assert projects.size() == 1
        assert projects[0].id == entity.id
        assert projects[0].name == entity.name
        assert projects[0].externalId == entity.externalId
    }

    def "should retrieve task list"() {
        given:
        ProjectEntity project = aRandom.projectEntity().build()
        projectRepository.save(project)

        TaskEntity task = aRandom.taskEntity().build()
        task.projectId = project.id;
        taskRepository.save(task)

        when:
        List<TaskDto> tasks = projectClient.getOpenTasksForProject(project.id.toString())

        then:
        assert tasks.size() == 1
        assert tasks[0].id == task.id
        assert tasks[0].name == task.name
        assert tasks[0].summary == task.summary
        assert tasks[0].externalId == task.externalId
    }



}

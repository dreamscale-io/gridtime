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
        assert projects[0].id == entity.id.toString()
        assert projects[0].name == entity.name
        assert projects[0].externalId == entity.externalId
    }

    def "should retrieve task list"() {
        given:
        TaskEntity entity = aRandom.taskEntity().build()
        taskRepository.save(entity)

        when:
        List<TaskDto> tasks = projectClient.getOpenTasksForProject("3")

        then:
        assert tasks.size() == 1
        assert tasks[0].id == entity.id.toString()
        assert tasks[0].name == entity.name
        assert tasks[0].summary == entity.summary
        assert tasks[0].externalId == entity.externalId
    }



}

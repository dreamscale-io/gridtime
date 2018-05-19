package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto
import com.dreamscale.htmflow.client.JournalClient
import com.dreamscale.htmflow.core.domain.ProjectEntity
import com.dreamscale.htmflow.core.domain.ProjectRepository
import com.dreamscale.htmflow.core.domain.TaskEntity
import com.dreamscale.htmflow.core.domain.TaskRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class JournalResourceSpec extends Specification {

    @Autowired
    JournalClient journalClient
    @Autowired
    ProjectRepository projectRepository
    @Autowired
    TaskRepository taskRepository

    def setup() {
        projectRepository.deleteAll()
        taskRepository.deleteAll()
    }

    def "should save new chunk"() {
        given:

        ProjectEntity project = aRandom.projectEntity().build()
        projectRepository.save(project)

        TaskEntity task = aRandom.taskEntity().forProject(project).build()
        taskRepository.save(task)

        ChunkEventInputDto chunkEvent = aRandom.chunkEventInputDto().forTask(task).build();

        when:
        ChunkEventOutputDto output = journalClient.createChunkEvent(chunkEvent)

        then:
        assert output != null
        assert output.getId() != null
        assert output.getTaskId() == chunkEvent.getTaskId()
        assert output.description == chunkEvent.getDescription()
    }


}

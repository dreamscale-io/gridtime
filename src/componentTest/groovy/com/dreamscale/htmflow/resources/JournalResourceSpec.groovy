package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.client.JournalClient
import com.dreamscale.htmflow.core.context.domain.ProjectRepository
import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.journal.ProjectDto
import com.dreamscale.htmflow.core.context.domain.ProjectEntity
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class JournalResourceSpec extends Specification {

    @Autowired
    JournalClient journalClient
    @Autowired
    ProjectRepository projectRepository

    def "should retrieve project list"() {
        given:
        ProjectEntity entity = aRandom.projectEntity().build()
        projectRepository.save(entity)

        when:
        List<ProjectDto> projects = journalClient.getProjects()

        then:
        assert projects.size() == 1
        assert projects[0].id == entity.id.toString()
        assert projects[0].name == entity.name
        assert projects[0].externalId == entity.externalId
    }

}

package org.dreamscale.htmflow.resources

import org.dreamscale.htmflow.ComponentTest
import org.dreamscale.htmflow.api.context.ProjectDto
import org.dreamscale.htmflow.client.ContextClient
import org.dreamscale.htmflow.core.context.domain.ProjectEntity
import org.dreamscale.htmflow.core.context.domain.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static org.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class ContextResourceSpec extends Specification {

    @Autowired
    ContextClient contextClient
    @Autowired
    ProjectRepository projectRepository

    def "should retrieve project list"() {
        given:
        ProjectEntity entity = aRandom.projectEntity().build()
        projectRepository.save(entity)

        when:
        List<ProjectDto> projects = contextClient.getProjects()

        then:
        assert projects.size() == 1
        assert projects[0].id == entity.id.toString()
        assert projects[0].name == entity.name
        assert projects[0].externalId == entity.externalId
    }

}

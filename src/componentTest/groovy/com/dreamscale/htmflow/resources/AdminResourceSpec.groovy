package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto
import com.dreamscale.htmflow.client.AdminClient
import com.dreamscale.htmflow.core.domain.ConfigProjectSyncRepository
import com.dreamscale.htmflow.core.domain.OrganizationEntity
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
public class AdminResourceSpec extends Specification {

    @Autowired
    AdminClient adminClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    ConfigProjectSyncRepository configProjectSyncRepository;

    def setup() {
        organizationRepository.deleteAll()
        configProjectSyncRepository.deleteAll()
    }

    def "should configure project sync for the org"() {

        given:
        OrganizationEntity organizationEntity = aRandom.organizationEntity().build()
        organizationRepository.save(organizationEntity)

        ProjectSyncInputDto syncDto = new ProjectSyncInputDto(organizationEntity.getId(), "jira_project")

        when:
        ProjectSyncOutputDto outputDto = adminClient.configProjectSync(syncDto)

        then:
        assert outputDto.id != null

    }

    def "should throw an error if the org does not exist"() {
        given:
        ProjectSyncInputDto syncDto = new ProjectSyncInputDto(UUID.randomUUID(), "jira_project")

        when:
        ProjectSyncOutputDto outputDto = adminClient.configProjectSync(syncDto)

        then:
        thrown(BadRequestException)
    }
}

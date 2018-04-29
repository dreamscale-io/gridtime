package com.dreamscale.htmflow.resources

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.api.status.Status
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@ComponentTest
class OrganizationResourceSpec extends Specification {

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    OrganizationRepository organizationRepository

    def setup() {
        organizationRepository.deleteAll()
    }

    def "should create organization with failed Jira connect"() {
        given:
        OrganizationInputDto organization = createOrganizationWithInvalidJira();

        when:
        OrganizationDto organizationDto = organizationClient.createOrganization(organization)

        then:
        assert organizationDto != null
        assert organizationDto.getId() != null
        assert organizationDto.getName() == organization.getName()
        assert organizationDto.getConnectionStatus() == Status.FAILED
        assert organizationDto.getConnectionFailedMessage() != null
        assert organizationDto.getInviteLink() == null
    }

    def "should create organization with valid connection"() {
        given:

        OrganizationInputDto organization = new OrganizationInputDto();
        organization.setName("DreamScale")
        organization.setJiraUser("janelle@dreamscale.io")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("9KC0iM24tfXf8iKDVP2q4198")

        when:
        OrganizationDto organizationDto = organizationClient.createOrganization(organization)

        then:
        assert organizationDto != null
        assert organizationDto.getId() != null
        assert organizationDto.getName() == organization.getName()
        assert organizationDto.getConnectionStatus() == Status.VALID
        assert organizationDto.getInviteLink() != null
    }

    def "should decode member invitation from link"() {
        given:
        OrganizationInputDto organization = createValidOrganization()

        when:
        OrganizationDto organizationDto = organizationClient.createOrganization(organization)
        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken());

        then:
        assert inviteOrg != null
        assert inviteOrg.getId() != null
        assert inviteOrg.getName() == organization.getName()
        assert inviteOrg.getConnectionStatus() == Status.VALID
        assert inviteOrg.getInviteLink() != null
    }



    private OrganizationInputDto createValidOrganization() {
        OrganizationInputDto organization = new OrganizationInputDto();
        organization.setName("DreamScale")
        organization.setJiraUser("janelle@dreamscale.io")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("9KC0iM24tfXf8iKDVP2q4198")

        return organization;
    }

    private OrganizationInputDto createOrganizationWithInvalidJira() {
        OrganizationInputDto organization = new OrganizationInputDto();
        organization.setName("DreamScale")
        organization.setJiraUser("fake")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("blabla")

        return organization;
    }

}

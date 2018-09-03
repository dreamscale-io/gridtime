package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.organization.OrganizationDto
import com.dreamscale.htmflow.api.organization.OrganizationInputDto
import com.dreamscale.htmflow.client.OrganizationClient
import com.dreamscale.htmflow.core.domain.OrganizationRepository
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import spock.lang.Specification;

@ComponentTest
public class JiraServiceIntegrationSpec extends Specification {

	JiraService jiraService

	@Autowired
	OrganizationClient organizationClient

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	JiraConnectionFactory jiraConnectionFactory;


	def setup() {
		organizationRepository.deleteAll()

		jiraService = new JiraService()
		jiraService.organizationRepository = organizationRepository
		jiraService.jiraConnectionFactory = jiraConnectionFactory;
	}

	def "should fetch a jira project by name"() {
		given:
		OrganizationDto validOrg = organizationClient.createOrganization(createValidOrganization());

		when:
		JiraProjectDto jiraProjectDto = jiraService.getProjectByName(validOrg.getId(), "flow-data-plugins")

		then:
		assert jiraProjectDto != null;
	}


	private OrganizationInputDto createValidOrganization() {
		OrganizationInputDto organization = new OrganizationInputDto();
		organization.setOrgName("DreamScale")
		organization.setDomainName("dreamscale.io")
		organization.setJiraUser("janelle@dreamscale.io")
		organization.setJiraSiteUrl("dreamscale.atlassian.net")
		organization.setJiraApiKey("9KC0iM24tfXf8iKDVP2q4198")

		return organization;
	}

}

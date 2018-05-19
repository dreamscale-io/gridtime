package com.dreamscale.htmflow.core.domain

import com.dreamscale.htmflow.api.organization.OrganizationInputDto

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomOrganizationEntityBuilder extends OrganizationEntity.OrganizationEntityBuilder {

	RandomOrganizationEntityBuilder() {
		id(aRandom.uuid())
				.orgName(aRandom.text(10))
				.domainName(aRandom.text(10))
				.jiraApiKey(aRandom.text(15))
				.jiraSiteUrl(aRandom.text(15))
				.jiraUser(aRandom.text(15))
	}

	public RandomOrganizationEntityBuilder withValidJira() {
		jiraUser("janelle@dreamscale.io")
		domainName("dreamscale.io")
		jiraSiteUrl("dreamscale.atlassian.net")
		jiraApiKey("9KC0iM24tfXf8iKDVP2q4198")
		return this
	}

}


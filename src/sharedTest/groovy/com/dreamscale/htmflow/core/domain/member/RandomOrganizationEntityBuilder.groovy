package com.dreamscale.htmflow.core.domain.member


import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomOrganizationEntityBuilder extends OrganizationEntity.OrganizationEntityBuilder {

	private OrganizationRepository organizationRepository

	RandomOrganizationEntityBuilder(OrganizationRepository organizationRepository) {
		this.organizationRepository = organizationRepository
		id(aRandom.uuid())
				.orgName(aRandom.text(10))
				.domainName(aRandom.text(10))
				.jiraApiKey(aRandom.text(15))
				.jiraSiteUrl(aRandom.text(15))
				.jiraUser(aRandom.text(15))
	}

	RandomOrganizationEntityBuilder withValidJira() {
		jiraUser("janelle@dreamscale.io")
		domainName("dreamscale.io")
		jiraSiteUrl("dreamscale.atlassian.net")
		jiraApiKey("9KC0iM24tfXf8iKDVP2q4198")
		return this
	}

	OrganizationEntity save() {
		organizationRepository.save(build())
	}

}


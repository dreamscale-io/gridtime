package com.dreamscale.htmflow.core.domain

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomOrganizationEntityBuilder extends OrganizationEntity.OrganizationEntityBuilder {

	RandomOrganizationEntityBuilder() {
		id(aRandom.uuid())
				.name(aRandom.text(10))
				.jiraApiKey(aRandom.text(15))
				.jiraSiteUrl(aRandom.text(15))
				.jiraUser(aRandom.text(15))
	}

}

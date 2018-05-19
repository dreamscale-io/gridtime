package com.dreamscale.htmflow.core.domain

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomProjectEntityBuilder extends ProjectEntity.ProjectEntityBuilder {

	RandomProjectEntityBuilder() {
		id(aRandom.uuid())
				.name(aRandom.text(10))
				.externalId(aRandom.numberText(5))
				.organizationId(aRandom.uuid())
	}

	RandomProjectEntityBuilder forOrg(OrganizationEntity organization) {
		organizationId(organization.getId())
		return this
	}
}

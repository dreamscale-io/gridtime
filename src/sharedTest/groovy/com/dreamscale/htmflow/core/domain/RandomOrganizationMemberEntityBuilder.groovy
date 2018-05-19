package com.dreamscale.htmflow.core.domain

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomOrganizationMemberEntityBuilder extends OrganizationMemberEntity.OrganizationMemberEntityBuilder {

	RandomOrganizationMemberEntityBuilder() {
		id(aRandom.uuid())
		.masterAccountId(aRandom.uuid())
		.organizationId(aRandom.uuid())
		.email(aRandom.email())
		.externalId(aRandom.text(4))
	}

	RandomOrganizationMemberEntityBuilder forOrg(OrganizationEntity organization) {
		organizationId(organization.getId())
		return this
	}

	RandomOrganizationMemberEntityBuilder forAccount(MasterAccountEntity masterAccountEntity) {
		masterAccountId(masterAccountEntity.getId())
		return this
	}

}


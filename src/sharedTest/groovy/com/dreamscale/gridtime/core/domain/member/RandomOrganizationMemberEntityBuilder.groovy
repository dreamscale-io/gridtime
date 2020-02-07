package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomOrganizationMemberEntityBuilder extends OrganizationMemberEntity.OrganizationMemberEntityBuilder {

	private OrganizationMemberRepository organizationMemberRepository

	RandomOrganizationMemberEntityBuilder(OrganizationMemberRepository organizationMemberRepository) {
		this.organizationMemberRepository = organizationMemberRepository
		id(aRandom.uuid())
		.rootAccountId(aRandom.uuid())
		.organizationId(aRandom.uuid())
		.email(aRandom.email())
		.username(aRandom.firstName().toLowerCase())
		.externalId(aRandom.text(4))
	}

	RandomOrganizationMemberEntityBuilder forOrg(OrganizationEntity organization) {
		organizationId(organization.getId())
		return this
	}

	RandomOrganizationMemberEntityBuilder forAccount(RootAccountEntity masterAccountEntity) {
		rootAccountId(masterAccountEntity.getId())
		return this
	}

	RandomOrganizationMemberEntityBuilder forOrgAndAccount(OrganizationEntity organization, RootAccountEntity masterAccountEntity) {
		forOrg(organization)
		forAccount(masterAccountEntity)
	}

	OrganizationMemberEntity save() {
		organizationMemberRepository.save(build())
	}

}


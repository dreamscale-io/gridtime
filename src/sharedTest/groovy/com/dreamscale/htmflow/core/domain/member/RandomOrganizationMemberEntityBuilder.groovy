package com.dreamscale.htmflow.core.domain.member


import static com.dreamscale.htmflow.core.CoreARandom.aRandom

class RandomOrganizationMemberEntityBuilder extends OrganizationMemberEntity.OrganizationMemberEntityBuilder {

	private OrganizationMemberRepository organizationMemberRepository

	RandomOrganizationMemberEntityBuilder(OrganizationMemberRepository organizationMemberRepository) {
		this.organizationMemberRepository = organizationMemberRepository
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

	RandomOrganizationMemberEntityBuilder forOrgAndAccount(OrganizationEntity organization, MasterAccountEntity masterAccountEntity) {
		forOrg(organization)
		forAccount(masterAccountEntity)
	}

	OrganizationMemberEntity save() {
		organizationMemberRepository.save(build())
	}

}


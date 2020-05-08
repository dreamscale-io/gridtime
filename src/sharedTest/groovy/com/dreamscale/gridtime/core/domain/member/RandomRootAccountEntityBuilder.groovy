package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomRootAccountEntityBuilder extends RootAccountEntity.RootAccountEntityBuilder {

	private RootAccountRepository rootAccountRepository

	RandomRootAccountEntityBuilder(RootAccountRepository rootAccountRepository) {
		this.rootAccountRepository = rootAccountRepository
		id(aRandom.uuid())
				.rootEmail(aRandom.email())
				.fullName(aRandom.name())
				.displayName(aRandom.name())
				.isEmailValidated(true)
				.registrationDate(aRandom.localDateTime())
				.rootUsername(aRandom.name().toLowerCase())
				.activationDate(aRandom.localDateTime())
				.isEmailValidated(true)
				.apiKey(aRandom.text(15))
	}

	RootAccountEntity save() {
		rootAccountRepository.save(build())
	}

}


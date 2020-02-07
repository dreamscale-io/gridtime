package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomRootAccountEntityBuilder extends RootAccountEntity.RootAccountEntityBuilder {

	private RootAccountRepository rootAccountRepository

	RandomRootAccountEntityBuilder(RootAccountRepository rootAccountRepository) {
		this.rootAccountRepository = rootAccountRepository
		id(aRandom.uuid())
				.rootEmail(aRandom.email())
				.fullName(aRandom.name())
				.activationCode(aRandom.text(15))
				.activationDate(aRandom.localDateTime())
				.apiKey(aRandom.text(15))
	}

	RootAccountEntity save() {
		rootAccountRepository.save(build())
	}

}


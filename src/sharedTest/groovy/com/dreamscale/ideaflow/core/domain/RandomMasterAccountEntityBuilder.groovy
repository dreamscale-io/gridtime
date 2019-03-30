package com.dreamscale.ideaflow.core.domain

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

class RandomMasterAccountEntityBuilder extends MasterAccountEntity.MasterAccountEntityBuilder {

	private MasterAccountRepository masterAccountRepository

    RandomMasterAccountEntityBuilder(MasterAccountRepository masterAccountRepository) {
		this.masterAccountRepository = masterAccountRepository
		id(aRandom.uuid())
				.masterEmail(aRandom.email())
				.fullName(aRandom.name())
				.activationCode(aRandom.text(15))
				.activationDate(aRandom.localDateTime())
				.apiKey(aRandom.text(15))
	}

	MasterAccountEntity save() {
		masterAccountRepository.save(build())
	}

}


package com.dreamscale.htmflow.core.domain.member

import com.dreamscale.htmflow.core.domain.member.MasterAccountEntity
import com.dreamscale.htmflow.core.domain.member.MasterAccountRepository

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

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


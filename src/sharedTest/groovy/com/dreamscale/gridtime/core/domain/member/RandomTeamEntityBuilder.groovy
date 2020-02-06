package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomTeamEntityBuilder extends TeamEntity.TeamEntityBuilder {

	private TeamRepository teamRepository

    RandomTeamEntityBuilder(TeamRepository teamRepository) {
		this.teamRepository = teamRepository
		id(aRandom.uuid())
				.name(aRandom.firstName())
				.organizationId(aRandom.uuid())
	}

	TeamEntity save() {
		teamRepository.save(build())
	}

}


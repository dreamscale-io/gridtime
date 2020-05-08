package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomTeamEntityBuilder extends TeamEntity.TeamEntityBuilder {

	private TeamRepository teamRepository

    RandomTeamEntityBuilder(TeamRepository teamRepository) {
		this.teamRepository = teamRepository

		String name = aRandom.firstName()
		id(aRandom.uuid())
				.name(name)
				.lowerCaseName(name.toLowerCase())
				.organizationId(aRandom.uuid())
				.creatorId(aRandom.uuid())
	}

	TeamEntity save() {
		teamRepository.save(build())
	}

}


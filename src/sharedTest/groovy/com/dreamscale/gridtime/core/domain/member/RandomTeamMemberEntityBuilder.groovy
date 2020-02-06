package com.dreamscale.gridtime.core.domain.member


import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomTeamMemberEntityBuilder extends TeamMemberEntity.TeamMemberEntityBuilder {

	private TeamMemberRepository teamMemberRepository

    RandomTeamMemberEntityBuilder(TeamMemberRepository teamMemberRepository) {
		this.teamMemberRepository = teamMemberRepository
		id(aRandom.uuid())
				.memberId(aRandom.uuid())
				.teamId(aRandom.uuid())
				.organizationId(aRandom.uuid())
	}

	TeamMemberEntity save() {
		teamMemberRepository.save(build())
	}

}


package com.dreamscale.gridtime.core.domain.work

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomTorchieFeedCursorEntityBuilder extends TorchieFeedCursorEntity.TorchieFeedCursorEntityBuilder {

	private TorchieFeedCursorRepository torchieFeedCursorRepository

    RandomTorchieFeedCursorEntityBuilder(TorchieFeedCursorRepository torchieFeedCursorRepository) {
		this.torchieFeedCursorRepository = torchieFeedCursorRepository
		id(aRandom.uuid())
				.torchieId(aRandom.uuid())
				.organizationId(aRandom.uuid())
				.firstTilePosition(aRandom.localDateTime())
				.lastPublishedDataCursor(aRandom.localDateTime())
				.nextWaitUntilCursor(aRandom.localDateTime())
				.claimingServerId(aRandom.uuid())
	}


	TorchieFeedCursorEntity save() {
		torchieFeedCursorRepository.save(build())
	}


}

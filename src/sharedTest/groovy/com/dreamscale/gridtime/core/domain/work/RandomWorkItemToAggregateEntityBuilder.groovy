package com.dreamscale.gridtime.core.domain.work

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

class RandomWorkItemToAggregateEntityBuilder extends WorkItemToAggregateEntity.WorkItemToAggregateEntityBuilder {

	private WorkItemToAggregateRepository workItemToAggregateRepository

    RandomWorkItemToAggregateEntityBuilder(WorkItemToAggregateRepository workItemToAggregateRepository) {
		this.workItemToAggregateRepository = workItemToAggregateRepository
		id(aRandom.uuid())
				.sourceTorchieId(aRandom.uuid())
				.teamId(aRandom.uuid())
				.zoomLevel(ZoomLevel.TWENTY)
				.tileSeq(aRandom.nextInt())
				.gridTime(GeometryClock.createGridTime(ZoomLevel.TWENTY, aRandom.localDateTime()).toDisplayString())
				.eventTime(aRandom.localDateTime())
				.processingState(ProcessingState.InProgress)
				.workToDoType(WorkToDoType.AggregateToTeam)
	}

	def forGridTime(GeometryClock.GridTime workAtThisTime) {
		zoomLevel(workAtThisTime.getZoomLevel())
		gridTime(workAtThisTime.toDisplayString())
	}

	WorkItemToAggregateEntity save() {
		workItemToAggregateRepository.save(build())
	}


}

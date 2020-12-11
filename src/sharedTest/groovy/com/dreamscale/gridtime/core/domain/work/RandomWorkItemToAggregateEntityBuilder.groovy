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
				.calendarId(aRandom.uuid())
				.gridTime(GeometryClock.createGridTime(ZoomLevel.TWENTY, aRandom.localDateTime()).toDisplayString())
				.eventTime(aRandom.localDateTime())
				.processingState(ProcessingState.InProgress)
				.workToDoType(WorkToDoType.AggregateToTeam)
	}

	WorkItemToAggregateEntity save() {
		workItemToAggregateRepository.save(build())
	}


}

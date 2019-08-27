package com.dreamscale.gridtime.core.domain.work;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.UUID;

public interface WorkReadyByTeamViewRepository extends CrudRepository<WorkReadyByTeamViewEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from work_ready_by_team_view " +
            "where team_size = tile_count "+
            "limit 1")
    WorkReadyByTeamViewEntity findOldestTeamCompleteWorkItem();


    @Query(nativeQuery = true, value = "select * from work_ready_by_team_view " +
            "where earliest_event_time <= (:clock) "+
            "limit 1")
    WorkReadyByTeamViewEntity findOldestPartialTeamWorkItemOlderThan(@Param("clock") Timestamp partialWorkReadyDate);


}

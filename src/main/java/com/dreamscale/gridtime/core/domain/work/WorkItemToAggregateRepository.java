package com.dreamscale.gridtime.core.domain.work;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.UUID;

public interface WorkItemToAggregateRepository extends CrudRepository<WorkItemToAggregateEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from work_item_to_aggregate " +
            "where processing_state = 'Ready' "+
            "order by event_time limit 1")
    WorkItemToAggregateEntity findOldestReadyWorkItem();


    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update work_item_to_aggregate " +
            "set processing_state = 'InProgress', "+
            " claiming_worker_id = cast(:workerId as uuid) "+
            "where calendar_id = cast(:calendarId as uuid) and team_id = cast(:teamId as uuid)")
    void updateInProgress(@Param("workerId") String workerId,
                          @Param("teamId") String teamId,
                          @Param("calendarId") String calendarId);


    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "delete from work_item_to_aggregate " +
            "where claiming_worker_id = cast(:workerId as uuid) " +
            "and processing_state = 'InProgress' ")
    void finishInProgressWorkItems(@Param("workerId") String workerId);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table work_item_to_aggregate")
    void truncate();
}

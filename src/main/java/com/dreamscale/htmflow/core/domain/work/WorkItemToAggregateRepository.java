package com.dreamscale.htmflow.core.domain.work;

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
            "where zoom_level = (:zoom) and tile_seq = (:tileseq) and team_id = cast(:teamId as uuid)")
    void updateInProgress(@Param("workerId") String workerId,
                          @Param("teamId") String teamId,
                          @Param("zoom") String zoomLevel,
                          @Param("tileseq") Long tileSeq);


    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "delete from work_item_to_aggregate " +
            "where claiming_worker_id = cast(:workerId as uuid) " +
            "and processing_state = 'InProgress' ")
    void finishInProgressWorkItems(@Param("workerId") String workerId);

    @Query(nativeQuery = true, value = "select pg_try_advisory_lock((:key)) ")
    boolean tryToAcquireLock(@Param("key") Long lockNumber);

    @Query(nativeQuery = true, value = "select pg_advisory_unlock((:key)) ")
    boolean releaseLock(@Param("key") Long lockNumber);

}

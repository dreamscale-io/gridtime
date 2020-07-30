package com.dreamscale.gridtime.core.domain.active;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ActiveAccountStatusRepository extends CrudRepository<ActiveAccountStatusEntity, UUID> {

    ActiveAccountStatusEntity findByRootAccountId(UUID id);

    ActiveAccountStatusEntity findByConnectionId(UUID connectionId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "select * from active_account_status " +
            "where last_activity < (:idleThreshold) and online_status = 'Online' ")
    List<ActiveAccountStatusEntity> findMissingHeartbeat(@Param("idleThreshold") Timestamp idleThreshold);


    @Query(nativeQuery = true, value = "select * from active_account_status " +
            "where last_activity < (:disconnectThreshold) "+
            "and online_status = 'Idle' ")
    List<ActiveAccountStatusEntity> findLongMissingHeartbeat(@Param("disconnectThreshold") Timestamp disconnectThreshold);

}

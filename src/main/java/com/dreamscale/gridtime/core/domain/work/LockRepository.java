package com.dreamscale.gridtime.core.domain.work;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.UUID;

public interface LockRepository extends CrudRepository<WorkItemToAggregateEntity, UUID> {

    @Query(nativeQuery = true, value = "select pg_try_advisory_lock((:key)) ")
    boolean tryToAcquireLock(@Param("key") Long lockNumber);

    @Query(nativeQuery = true, value = "select pg_advisory_unlock((:key)) ")
    boolean releaseLock(@Param("key") Long lockNumber);

}

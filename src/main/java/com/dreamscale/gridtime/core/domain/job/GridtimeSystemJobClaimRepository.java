package com.dreamscale.gridtime.core.domain.job;

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GridtimeSystemJobClaimRepository extends CrudRepository<GridtimeSystemJobClaimEntity, UUID> {

    GridtimeSystemJobClaimEntity findById(UUID id);

    @Query(nativeQuery = true, value = "select * from gridtime_system_job_claim " +
            "where job_type =(:jobType) " +
            "and job_status = 'IN_PROGRESS' ")
    GridtimeSystemJobClaimEntity findInProgressJobsByJobType(@Param("jobType") String jobType);


    @Query(nativeQuery = true, value = "select * from gridtime_system_job_claim " +
            "where claiming_worker_id =(:workerId) and job_status = 'IN_PROGRESS' " )
    GridtimeSystemJobClaimEntity findInProgressJobByClaimingWorkerId(@Param("workerId") UUID workerId);

}



package com.dreamscale.htmflow.core.domain.flow;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FlowEventRepository extends CrudRepository<FlowEventEntity, Long> {

    @Query(nativeQuery = true, value = "select * from flow_activity " +
            "where member_id =(:memberId) " +
            "and (time_position between (:start) and (:end)) " +
            "order by id")
    List<FlowEventEntity> findByRange(@Param("memberId") UUID memberId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);


}

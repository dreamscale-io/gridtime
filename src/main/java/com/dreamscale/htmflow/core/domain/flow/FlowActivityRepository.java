
package com.dreamscale.htmflow.core.domain.flow;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FlowActivityRepository extends PagingAndSortingRepository<FlowActivityEntity, Long> {

	@Query(nativeQuery = true, value = "select * from flow_activity " +
			"where member_id =(:memberId) " +
			"and (start_time between (:start) and (:end) or end_time between (:start) and (:end)) " +
			"order by id")
	List<FlowActivityEntity> findByRange(@Param("memberId") UUID memberId,
										 @Param("start") LocalDateTime start,
										 @Param("end") LocalDateTime end);

	List<FlowActivityEntity> findByMemberId(UUID memberId);

	FlowActivityEntity findFirst1ByMemberIdOrderByEndDesc(UUID memberId);

	@Query(nativeQuery = true, value = "select * from flow_activity " +
			"where member_id=(:memberId) " +
			"and start_time >= (:afterDate) " +
			"and id >= (:sequence) " +
			"and activity_type in ('Editor', 'Modification') "+
			"order by start_time, id asc limit (:limit)")
    List<FlowActivityEntity> findFileActivityByMemberIdAfterDateWithLimit(@Param("memberId") UUID memberId,
																		  @Param("afterDate") Timestamp afterDate,
																		  @Param("sequence") Long sequence,
																		  @Param("limit") int limit);

	@Query(nativeQuery = true, value = "select * from flow_activity " +
			"where member_id=(:memberId) " +
			"and start_time >= (:afterDate) " +
			"and id >= (:sequence) " +
			"and activity_type in ('Execution') "+
			"order by start_time, id asc limit (:limit)")
	List<FlowActivityEntity> findExecutionActivityByMemberIdAfterDateWithLimit(@Param("memberId") UUID memberId,
																		  @Param("afterDate") Timestamp afterDate,
																		  @Param("sequence") Long sequence,
																		  @Param("limit") int limit);

}

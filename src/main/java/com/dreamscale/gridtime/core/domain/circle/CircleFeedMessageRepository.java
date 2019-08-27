package com.dreamscale.gridtime.core.domain.circle;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface CircleFeedMessageRepository extends CrudRepository<CircleFeedMessageEntity, UUID> {

    List<CircleFeedMessageEntity> findByCircleIdOrderByPosition(UUID circleId);

    @Query(nativeQuery = true, value = "select * from circle_feed_message_view cf " +
            "where exists (select 1 from circle c where c.owner_member_id=(:memberId) and c.id=cf.circle_id) " +
            "and position >= (:afterDate) " +
            "order by position asc limit (:limit)")
    List<CircleFeedMessageEntity> findByOwnerIdAfterDateWithLimit(@Param("memberId") UUID memberId,
                                                                  @Param("afterDate") Timestamp afterDate,
                                                                  @Param("limit") int limit);


}

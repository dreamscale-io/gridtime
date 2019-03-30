package com.dreamscale.ideaflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CircleContextRepository extends CrudRepository<CircleContextEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from circle_context " +
            "where circle_id=(:circleId) " +
            "order by position desc limit 1")
    CircleContextEntity findLastByCircleId(@Param("circleId") UUID circleId);

    @Query(nativeQuery = true, value = "select * from circle_context " +
            "where circle_id=(:circleId) " +
            "order by position asc limit 1")
    CircleContextEntity findFirstByCircleId(@Param("circleId") UUID circleId);
}

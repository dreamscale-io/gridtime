package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CircleRepository extends CrudRepository<CircleEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from circle c " +
            "where c.organization_id = (:organizationId) " +
            "and c.end_time is null " +
            "order by c.start_time ")
    List<CircleEntity> findAllOpenCirclesForOrganization(@Param("organizationId") UUID organizationId);


    @Query(nativeQuery = true, value = "select * from circle c " +
            "where c.organization_id = (:organizationId) " +
            "and c.owner_member_id = (:memberId) "+
            "and c.end_time is null " +
            "and c.on_shelf = true " +
            "order by c.start_time ")
    List<CircleEntity> findAllDoItLaterCircles(@Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId);

    @Query(nativeQuery = true, value = "select * from circle c " +
            "where c.organization_id = (:organizationId) " +
            "and exists (select 1 from circle_member cm " +
            "where c.id = cm.circle_id " +
            "and cm.spirit_id = (:spiritId)) " +
            "order by c.start_time ")
    List<CircleEntity> findAllByParticipation(@Param("organizationId") UUID organizationId, @Param("spiritId") UUID spiritId);


    CircleEntity findByOwnerMemberIdAndId(UUID ownerMemberId, UUID id);
}

package com.dreamscale.ideaflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberNameRepository extends CrudRepository<MemberNameEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from member_name_view m " +
            "where exists (select 1 from circle_member cm " +
            "where cm.spirit_id = m.spirit_id "+
            "and cm.circle_id = (:circleId))")
    List<MemberNameEntity> findAllByCircleId(@Param("circleId") UUID circleId);

    MemberNameEntity findBySpiritId(UUID spiritId);

}

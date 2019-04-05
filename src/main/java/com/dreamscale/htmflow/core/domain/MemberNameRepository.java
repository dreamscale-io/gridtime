package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberNameRepository extends CrudRepository<MemberNameEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from member_name_view m " +
            "where exists (select 1 from circle_member cm " +
            "where cm.torchie_id = m.torchie_id "+
            "and cm.circle_id = (:circleId))")
    List<MemberNameEntity> findAllByCircleId(@Param("circleId") UUID circleId);

    MemberNameEntity findByTorchieId(UUID spiritId);

}

package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberDetailsRepository extends CrudRepository<MemberDetailsEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from member_details_view m " +
            "where exists (select 1 from talk_room_member rm " +
            "where rm.member_id = m.member_id "+
            "and rm.room_id = (:roomId))")
    List<MemberDetailsEntity> findAllByRoomId(@Param("roomId") UUID roomId);

    MemberDetailsEntity findByOrganizationIdAndMemberId(UUID organizationId, UUID memberId);

    MemberDetailsEntity findByMemberId(UUID memberId);

}

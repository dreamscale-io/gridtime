package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberNameRepository extends CrudRepository<MemberNameEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from member_name_view m " +
            "where exists (select 1 from talk_room_member rm " +
            "where rm.member_id = m.member_id "+
            "and rm.room_id = (:roomId))")
    List<MemberNameEntity> findAllByRoomId(@Param("roomId") UUID roomId);

    MemberNameEntity findByMemberId(UUID memberId);

}

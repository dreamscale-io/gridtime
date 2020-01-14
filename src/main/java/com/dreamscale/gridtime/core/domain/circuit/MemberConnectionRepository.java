package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberConnectionRepository extends CrudRepository<MemberConnectionEntity, UUID> {


    MemberConnectionEntity findByOrganizationIdAndMemberId(UUID organizationId, UUID memberId);


    @Query(nativeQuery = true, value = "select mc.* from member_connection_view mc, talk_room_member trm " +
            "where trm.organization_id = (:organizationId) " +
            "and mc.member_id = trm.member_id " +
            "and trm.room_id = (:roomId) ")
    List<MemberConnectionEntity> findByConnectionsInTalkRoom(@Param("organizationId") UUID organizationId, @Param("roomId") UUID roomId);

    MemberConnectionEntity findByConnectionId(UUID connectionId);
}

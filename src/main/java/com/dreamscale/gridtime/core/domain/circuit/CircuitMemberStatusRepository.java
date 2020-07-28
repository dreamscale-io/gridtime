package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CircuitMemberStatusRepository extends CrudRepository<CircuitMemberStatusEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from circuit_member_status_view " +
            "where circuit_id = (:circuitId) " +
            "and organization_id = (:organizationId)  " +
            "and is_active_in_session = true ")
    List<CircuitMemberStatusEntity> findActiveMembersByCircuitId(@Param("organizationId") UUID organizationId,
                                                                 @Param("circuitId") UUID circuitId);



    List<CircuitMemberStatusEntity> findByOrganizationIdAndCircuitId(UUID organizationId, UUID circuitId);

    CircuitMemberStatusEntity findByOrganizationIdAndCircuitIdAndMemberId(UUID organizationId, UUID circuitId, UUID memberId);
}

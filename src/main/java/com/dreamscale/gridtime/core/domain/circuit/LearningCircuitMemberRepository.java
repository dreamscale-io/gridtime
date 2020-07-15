package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LearningCircuitMemberRepository extends CrudRepository<LearningCircuitMemberEntity, UUID> {


    LearningCircuitMemberEntity findByOrganizationIdAndCircuitIdAndMemberId(UUID organizationId, UUID circuitId, UUID memberId);

    List<LearningCircuitMemberEntity> findByOrganizationIdAndCircuitId(UUID organizationId, UUID circuitId);

    @Modifying
    @Query(nativeQuery = true, value = "update learning_circuit_member lcm " +
            "set is_active_in_session = false " +
            "where lcm.circuit_id = (:circuitId) " +
            "and lcm.organization_id = (:organizationId)  " +
            "and lcm.member_id != (:ownerId) ")
    void updateAllMembersToInactiveExceptOwner(@Param("organizationId") UUID organizationId,
                                               @Param("circuitId") UUID circuitId,
                                               @Param("ownerId") UUID ownerId);
}

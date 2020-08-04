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

    @Modifying
    @Query(nativeQuery = true, value = "update learning_circuit_member lcm " +
            "set is_active_in_session = false " +
            "where lcm.circuit_id = (:circuitId) " +
            "and lcm.organization_id = (:organizationId)  " +
            "and lcm.member_id = (:memberId) ")
    void updateMemberToInactive(@Param("organizationId") UUID organizationId,
                                @Param("circuitId") UUID circuitId,
                                @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select count(*) from learning_circuit_member lcm " +
            "where lcm.circuit_id = (:circuitId) " +
            "and lcm.join_state = (:joinState) ")
    long countByCircuitIdAndJoinState(@Param("circuitId") UUID circuitId, @Param("joinState") String state);

    @Query(nativeQuery = true, value = "select count(*) from learning_circuit_member lcm " +
            "where lcm.circuit_id = (:circuitId) " +
            "and lcm.join_state = 'TROUBLESHOOT' ")
    long countWTFMembersByCircuitId(@Param("circuitId") UUID circuitId);

    @Query(nativeQuery = true, value = "select count(*) from learning_circuit_member lcm " +
            "where lcm.circuit_id = (:circuitId) ")
    long countByCircuitId(@Param("circuitId") UUID circuitId);
}

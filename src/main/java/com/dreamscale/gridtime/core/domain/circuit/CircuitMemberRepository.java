package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CircuitMemberRepository extends CrudRepository<CircuitMemberEntity, UUID> {


    CircuitMemberEntity findByOrganizationIdAndCircuitIdAndMemberId(UUID organizationId, UUID circuitId, UUID memberId);

    List<CircuitMemberEntity> findByOrganizationIdAndCircuitId(UUID organizationId, UUID circuitId);

    @Modifying
    @Query(nativeQuery = true, value = "update circuit_member cm " +
            "set is_active_in_session = false " +
            "where cm.circuit_id = (:circuitId) " +
            "and cm.organization_id = (:organizationId)  " +
            "and cm.member_id != (:ownerId) ")
    void updateAllMembersToInactiveExceptOwner(@Param("organizationId") UUID organizationId,
                                               @Param("circuitId") UUID circuitId,
                                               @Param("ownerId") UUID ownerId);

    @Modifying
    @Query(nativeQuery = true, value = "update circuit_member cm " +
            "set is_active_in_session = false " +
            "where cm.circuit_id = (:circuitId) " +
            "and cm.organization_id = (:organizationId)  " +
            "and cm.member_id = (:memberId) ")
    void updateMemberToInactive(@Param("organizationId") UUID organizationId,
                                @Param("circuitId") UUID circuitId,
                                @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select count(*) from circuit_member cm " +
            "where cm.circuit_id = (:circuitId) " +
            "and cm.join_state = (:joinState) ")
    long countByCircuitIdAndJoinState(@Param("circuitId") UUID circuitId, @Param("joinState") String state);

    @Query(nativeQuery = true, value = "select count(*) from circuit_member cm " +
            "where cm.circuit_id = (:circuitId) " +
            "and cm.join_state = 'TROUBLESHOOT' ")
    long countWTFMembersByCircuitId(@Param("circuitId") UUID circuitId);

    @Query(nativeQuery = true, value = "select count(*) from circuit_member cm " +
            "where cm.circuit_id = (:circuitId) ")
    long countByCircuitId(@Param("circuitId") UUID circuitId);
}

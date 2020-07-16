package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LearningCircuitRepository extends CrudRepository<LearningCircuitEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and c.owner_id = (:memberId) "+
            "and c.circuit_state = 'TROUBLESHOOT' " +
            "order by c.open_time ")
    List<LearningCircuitEntity> findAllActiveCircuitsOwnedBy(@Param("organizationId") UUID organizationId,
                                                             @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and c.owner_id = (:memberId) "+
            "and c.circuit_state = 'ONHOLD' " +
            "order by c.open_time ")
    List<LearningCircuitEntity> findAllOnHoldCircuitsOwnedBy(@Param("organizationId") UUID organizationId,
                                                             @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where exists (select 1 from learning_circuit_member lcm " +
            "where lcm.circuit_id = c.id " +
            "and lcm.member_id = (:memberId) ) "+
            "and (c.circuit_state = 'SOLVED' OR c.circuit_state = 'RETRO') " +
            "and c.organization_id = (:organizationId) " +
            "order by c.total_circuit_elapsed_nano_time desc ")
    List<LearningCircuitEntity> findReadyForRetroCircuits(@Param("organizationId") UUID organizationId,
                                                          @Param("memberId") UUID memberId);



    LearningCircuitEntity findByOrganizationIdAndCircuitName(UUID organizationId, String circuitName);

    LearningCircuitEntity findByOrganizationIdAndOwnerIdAndCircuitName(UUID organizationId, UUID ownerId, String circuitName);


    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and exists (select 1 from talk_room tr " +
            "where tr.room_name = (:roomName) " +
            "and (tr.id = c.wtf_room_id or tr.id=c.retro_room_id)) ")
    LearningCircuitEntity findCircuitByOrganizationAndRoomName(@Param("organizationId")UUID organizationId,
                                                               @Param("roomName") String roomName);


    LearningCircuitEntity findByOrganizationIdAndId(UUID organizationId, UUID circuitId);

        @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and c.owner_id != (:memberId) " +
            "and exists (select 1 from learning_circuit_member lcm " +
            "where lcm.circuit_id = c.id " +
            "and lcm.member_id = (:memberId) " +
            "and lcm.is_active_in_session = true ) "+
            "and (c.circuit_state = 'TROUBLESHOOT' or c.circuit_state = 'RETRO') " +
            "order by c.open_time ")
    List<LearningCircuitEntity> findAllParticipatingCircuits(@Param("organizationId") UUID organizationId,
                                                             @Param("memberId") UUID memberId);


//
//    @Query(nativeQuery = true, value = "select * from circle c " +
//            "where c.organization_id = (:organizationId) " +
//            "and c.end_time is null " +
//            "order by c.start_time ")
//    List<CircleEntity> findAllOpenCircuitsForOrganization(@Param("organizationId") UUID organizationId);
//
//
//    @Query(nativeQuery = true, value = "select * from circle c " +
//            "where c.organization_id = (:organizationId) " +
//            "and c.owner_member_id = (:memberId) "+
//            "and c.end_time is null " +
//            "and c.on_shelf = true " +
//            "order by c.start_time ")
//    List<CircleEntity> findAllDoItLaterCircuits(@Param("organizationId") UUID organizationId, @Param("fromMemberId") UUID memberId);
//
//    @Query(nativeQuery = true, value = "select * from circle c " +
//            "where c.organization_id = (:organizationId) " +
//            "and exists (select 1 from circle_member cm " +
//            "where c.id = cm.circle_id " +
//            "and cm.torchie_id = (:torchieId)) " +
//            "order by c.start_time ")
//    List<CircleEntity> findAllCircuitsByParticipation(@Param("organizationId") UUID organizationId, @Param("torchieId") UUID torchieId);
//
//



}

package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
            "where exists (select 1 from circuit_member cm " +
            "where cm.circuit_id = c.id " +
            "and cm.member_id = (:memberId) ) "+
            "and (c.circuit_state = 'RETRO') " +
            "and c.organization_id = (:organizationId) " +
            "order by c.total_circuit_elapsed_nano_time desc ")
    List<LearningCircuitEntity> findParticipatingRetroCircuits(@Param("organizationId") UUID organizationId,
                                                               @Param("memberId") UUID memberId);

    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where exists (select 1 from circuit_member cm " +
            "where cm.circuit_id = c.id " +
            "and cm.member_id = (:memberId) ) "+
            "and (c.circuit_state = 'SOLVED') " +
            "and c.organization_id = (:organizationId) " +
            "order by c.total_circuit_elapsed_nano_time desc ")
    List<LearningCircuitEntity> findParticipatingSolvedCircuits(@Param("organizationId") UUID organizationId,
                                                                @Param("memberId") UUID memberId);


    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and exists (select 1 from circuit_member cm " +
            "where cm.circuit_id = c.id " +
            "and cm.member_id = (:memberId) " +
            "and cm.is_active_in_session = true ) "+
            "and (c.circuit_state = 'TROUBLESHOOT') " +
            "order by c.open_time ")
    List<LearningCircuitEntity> findParticipatingTroubleshootCircuits(@Param("organizationId") UUID organizationId,
                                                                      @Param("memberId") UUID memberId);

    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and exists (select 1 from circuit_member cm " +
            "where cm.circuit_id = c.id " +
            "and cm.member_id = (:memberId) " +
            "and cm.is_active_in_session = true ) "+
            "and (c.circuit_state = 'TROUBLESHOOT' or c.circuit_state = 'RETRO') " +
            "order by c.open_time ")
    List<LearningCircuitEntity> findAllParticipatingCircuits(@Param("organizationId") UUID organizationId,
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


    @Query(nativeQuery = true, value = "select * from learning_circuit where id=(:circuitId) for update ")
    LearningCircuitEntity selectForUpdate(@Param("circuitId") UUID circuitId);


    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and c.owner_id = (:memberId) " +
            "and c.solved_time is not null " +
            "and c.open_time between (:startTime) and (:endTime) " +
            "order by total_circuit_elapsed_nano_time desc limit (:limit) ")
    List<LearningCircuitEntity> findTopWTFsForMemberInTimeRange(
            @Param("organizationId") UUID organizationId, @Param("memberId") UUID memberId,
            @Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime, @Param("limit") int limit);


    @Query(nativeQuery = true, value = "select * from learning_circuit c " +
            "where c.organization_id = (:organizationId) " +
            "and c.owner_id in (select tm.member_id from team_member tm where tm.team_id = (:teamId)) " +
            "and c.solved_time is not null " +
            "and c.open_time between (:startTime) and (:endTime) " +
            "order by total_circuit_elapsed_nano_time desc limit (:limit) ")
    List<LearningCircuitEntity> findTopWTFsAcrossTeamInTimeRange(
            @Param("organizationId") UUID organizationId, @Param("teamId") UUID teamId,
            @Param("startTime") Timestamp startTime, @Param("endTime")  Timestamp endTime, @Param("limit") int limit);
}

package com.dreamscale.gridtime.core.domain.terminal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TerminalCircuitQueryTargetRepository extends CrudRepository<TerminalCircuitQueryTargetEntity, UUID> {


    @Query(nativeQuery = true, value = "select qt.* from terminal_circuit_query_target qt " +
            "where exists (select 1 from terminal_circuit tc " +
            "where tc.organization_id = (:organizationId) and tc.circuit_name = (:circuitName) and tc.id = lh.circuit_id) "+
            "order by target_date desc limit 1")
    TerminalCircuitQueryTargetEntity findLastTargetByOrganizationIdAndCircuitName(@Param("organizationId") UUID organizationId,
                                                                                  @Param("circuitName") String circuitName);

    @Query(nativeQuery = true, value = "select qt.* from terminal_circuit_query_target qt " +
            "where qt.circuit_id = (:circuitId) " +
            "order by target_date desc limit 1")
    TerminalCircuitQueryTargetEntity findLastTargetByCircuitId(@Param("circuitId") UUID circuitId);
}

package com.dreamscale.gridtime.core.domain.terminal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TerminalCircuitLocationHistoryRepository extends CrudRepository<TerminalCircuitLocationHistoryEntity, UUID> {


    @Query(nativeQuery = true, value = "select lh.* from terminal_circuit_location_history lh " +
            "where exists (select 1 from terminal_circuit tc " +
            "where tc.organization_id = (:organizationId) and tc.circuit_name = (:circuitName) and tc.id = lh.circuit_id) "+
            "order by movement_date desc limit 1")
    TerminalCircuitLocationHistoryEntity findLastLocationByOrganizationIdAndCircuitName(
            @Param("organizationId") UUID organizationId, @Param("circuitName") String circuitName);
}

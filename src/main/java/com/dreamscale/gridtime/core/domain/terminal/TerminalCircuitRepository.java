package com.dreamscale.gridtime.core.domain.terminal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TerminalCircuitRepository extends CrudRepository<TerminalCircuitEntity, UUID> {

    TerminalCircuitEntity findByOrganizationIdAndCircuitName(UUID organizationId, String circuitName);
}

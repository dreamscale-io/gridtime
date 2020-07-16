package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CircuitMarkRepository extends CrudRepository<CircuitMarkEntity, UUID> {

    CircuitMarkEntity findByOrganizationIdAndMemberIdAndCircuitId(UUID organizationId, UUID memberId, UUID circuitId);
}

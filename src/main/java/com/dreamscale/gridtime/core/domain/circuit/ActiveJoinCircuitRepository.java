package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ActiveJoinCircuitRepository extends CrudRepository<ActiveJoinCircuitEntity, UUID> {


    ActiveJoinCircuitEntity findByOrganizationIdAndMemberId(UUID organizationId, UUID memberId);
}

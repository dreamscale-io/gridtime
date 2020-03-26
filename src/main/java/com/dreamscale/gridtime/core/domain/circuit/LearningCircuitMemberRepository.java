package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface LearningCircuitMemberRepository extends CrudRepository<LearningCircuitMemberEntity, UUID> {


    LearningCircuitMemberEntity findByOrganizationIdAndCircuitIdAndMemberId(UUID organizationId, UUID circuitId, UUID memberId);

    List<LearningCircuitMemberEntity> findByCircuitId(UUID circuitId);
}

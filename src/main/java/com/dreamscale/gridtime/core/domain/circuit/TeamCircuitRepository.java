package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TeamCircuitRepository extends CrudRepository<TeamCircuitEntity, UUID> {

    TeamCircuitEntity findByTeamId(UUID teamId);

    @Query(nativeQuery = true, value = "select * from team_circuit tc, team t " +
            "where tc.organization_id = (:organizationId) " +
            "and tc.team_id = t.id "+
            "and t.name = (:teamName) ")
    public TeamCircuitEntity findByOrganizationIdAndTeamName(@Param("organizationId") UUID organizationId,
                                                @Param("teamName") String teamName);

    TeamCircuitEntity findByOrganizationIdAndTeamId(UUID organizationId, UUID teamId);
}

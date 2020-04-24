package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.core.domain.dictionary.TeamBookEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrganizationSubscriptionRepository extends CrudRepository<OrganizationSubscriptionEntity, UUID> {

    List<OrganizationSubscriptionEntity> findByRootAccountOwnerIdOrderByCreationDate(UUID rootAccountId);

    OrganizationSubscriptionEntity findByOrganizationId(UUID organizationId);


    @Query(nativeQuery = true, value = "select * from organization_subscription " +
            "where organization_id = (:organizationId) for update")
    OrganizationSubscriptionEntity selectOrgSubscriptionForUpdate(@Param("organizationId") UUID organizationId);
}

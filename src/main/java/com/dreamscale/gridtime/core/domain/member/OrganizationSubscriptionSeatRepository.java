package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrganizationSubscriptionSeatRepository extends CrudRepository<OrganizationSubscriptionSeatEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from organization_subscription_seat " +
            "where organization_id = (:organizationId) and root_account_id = (:rootAccountId) " +
            "and subscription_status = 'VALID' for update")
    OrganizationSubscriptionSeatEntity selectValidSubscriptionSeatForUpdate(@Param("organizationId") UUID organizationId,
                                                                            @Param("rootAccountId") UUID rootAccountId);
}

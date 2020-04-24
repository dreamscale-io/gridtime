package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationSubscriptionDetailsRepository extends CrudRepository<OrganizationSubscriptionDetailsEntity, UUID> {

    List<OrganizationSubscriptionDetailsEntity> findByRootAccountOwnerIdOrderByCreationDate(UUID rootAccountId);
}

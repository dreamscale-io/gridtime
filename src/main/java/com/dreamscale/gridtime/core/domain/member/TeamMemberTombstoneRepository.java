package com.dreamscale.gridtime.core.domain.member;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamMemberTombstoneRepository extends CrudRepository<TeamMemberTombstoneEntity, UUID> {

}

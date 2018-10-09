package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MemberXPRepository extends CrudRepository<MemberXPEntity, UUID> {

    MemberXPEntity findByMemberId(UUID memberId);

}

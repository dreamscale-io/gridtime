package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MemberNameRepository extends CrudRepository<MemberNameEntity, UUID> {
}

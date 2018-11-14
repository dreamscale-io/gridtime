package com.dreamscale.htmflow.core.domain;

import com.dreamscale.htmflow.core.domain.TeamMemberWorkStatusEntity;
import com.dreamscale.htmflow.core.domain.WtfSessionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface WtfSessionRepository extends CrudRepository<WtfSessionEntity, UUID> {


}

package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface WtfJournalEntryRepository extends CrudRepository<WtfJournalLinkEntity, UUID> {

}

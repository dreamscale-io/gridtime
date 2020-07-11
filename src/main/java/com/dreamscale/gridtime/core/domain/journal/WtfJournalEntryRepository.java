package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface WtfJournalEntryRepository extends CrudRepository<WtfJournalEntryEntity, UUID> {

}

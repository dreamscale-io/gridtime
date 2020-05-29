package com.dreamscale.gridtime.api.circuit;

import com.dreamscale.gridtime.api.journal.IntentionDto;
import com.dreamscale.gridtime.api.journal.JournalEntryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentionStartedDetailsDto implements MessageDetailsBody {

    String username;
    UUID memberId;
    JournalEntryDto journalEntry;
}

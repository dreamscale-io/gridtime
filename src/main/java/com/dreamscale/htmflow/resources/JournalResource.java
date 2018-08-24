package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.journal.IntentionInputDto;
import com.dreamscale.htmflow.api.journal.IntentionOutputDto;
import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.JOURNAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JournalResource {

    @Autowired
    private JournalService journalService;

    @PostMapping(ResourcePaths.INTENTION_PATH)
    IntentionOutputDto createIntention(@RequestBody IntentionInputDto intentionInput) {
        RequestContext context = RequestContext.get();
        return journalService.createIntention(context.getMasterAccountId(), intentionInput);
    }

    @GetMapping(ResourcePaths.INTENTION_PATH)
    List<IntentionOutputDto> getIntentions(@RequestParam("from_date") String fromDate, @RequestParam("to_date") String toDate) {
        RequestContext context = RequestContext.get();

        return journalService.getIntentionsWithinRange(context.getMasterAccountId(), null, null);
    }


    @GetMapping(ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH)
    List<IntentionOutputDto> getRecentIntentionsForMember(@RequestParam("member") Optional<String> memberId) {
        RequestContext context = RequestContext.get();
        if (memberId.isPresent()) {
            return journalService.getRecentIntentionsForMember(context.getMasterAccountId(), UUID.fromString(memberId.get()));
        } else {
            return journalService.getRecentIntentions(context.getMasterAccountId());
        }
    }

}

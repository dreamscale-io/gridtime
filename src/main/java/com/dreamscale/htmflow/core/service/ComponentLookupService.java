package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.activity.*;
import com.dreamscale.htmflow.api.batch.NewBatchEvent;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.EventType;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.domain.flow.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ComponentLookupService {

    @Autowired
    FlowActivityRepository flowActivityRepository;


    void populateComponentsForBatch(UUID projectId, NewEditorActivity activity) {
        //I want to actually assign this component for all the entries,
        // based on the time sequence order of the last file activity
        // take the input sequence for these, sort them, and process, save, one at a time.

        //I need to integrate this with a sequencing of the whole thing, take a batch,
        //sort, and process the entries one by one
        //run existing set of tests

        //so if I just did a FileActivity, the next execution activity should follow the same component

        //
    }

}

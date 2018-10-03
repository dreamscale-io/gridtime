package com.dreamscale.htmflow.api.batch;

import com.dreamscale.htmflow.api.activity.NewEditorActivity;
import com.dreamscale.htmflow.api.activity.NewExecutionActivity;
import com.dreamscale.htmflow.api.activity.NewExternalActivity;
import com.dreamscale.htmflow.api.activity.NewIdleActivity;
import com.dreamscale.htmflow.api.activity.NewModificationActivity;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewFlowBatch {

    private LocalDateTime timeSent;

    @Singular("editorActivity")
    private List<NewEditorActivity> editorActivityList;
    @Singular("externalActivity")
    private List<NewExternalActivity> externalActivityList;
    @Singular("idleActivity")
    private List<NewIdleActivity> idleActivityList;
    @Singular("executionActivity")
    private List<NewExecutionActivity> executionActivityList;
    @Singular("modificationActivity")
    private List<NewModificationActivity> modificationActivityList;
    @Singular("event")
    private List<NewBatchEvent> eventList;

    private List<List> getBatchItemLists() {
        ArrayList<List> batchItemLists = new ArrayList<>(10);
        batchItemLists.add(getNotNullList(editorActivityList));
        batchItemLists.add(getNotNullList(externalActivityList));
        batchItemLists.add(getNotNullList(idleActivityList));
        batchItemLists.add(getNotNullList(executionActivityList));
        batchItemLists.add(getNotNullList(modificationActivityList));
        batchItemLists.add(getNotNullList(eventList));
        return batchItemLists;
    }

    private List getNotNullList(List list) {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    @JsonIgnore
    public List getBatchItems() {
        List<Object> batchItems = new ArrayList<>();
        getBatchItemLists().forEach(batchItems::addAll);
        return batchItems;
    }

    @JsonIgnore
    public boolean isEmpty() {
        for (List list : getBatchItemLists()) {
            if (list.isEmpty() == false) {
                return false;
            }
        }
        return true;
    }

}

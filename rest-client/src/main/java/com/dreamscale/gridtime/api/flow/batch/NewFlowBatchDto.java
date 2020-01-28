package com.dreamscale.gridtime.api.flow.batch;

import com.dreamscale.gridtime.api.flow.activity.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewFlowBatchDto {

    private LocalDateTime timeSent;

    @Singular("editorActivity")
    private List<NewEditorActivityDto> editorActivityList;
    @Singular("externalActivity")
    private List<NewExternalActivityDto> externalActivityList;
    @Singular("idleActivity")
    private List<NewIdleActivityDto> idleActivityList;
    @Singular("executionActivity")
    private List<NewExecutionActivityDto> executionActivityList;
    @Singular("modificationActivity")
    private List<NewModificationActivityDto> modificationActivityList;
    @Singular("event")
    private List<NewFlowBatchEventDto> eventList;


    public List getAllBatchActivity() {
        List batchActivity = new ArrayList<>();
        batchActivity.addAll(getNotNullList(editorActivityList));
        batchActivity.addAll(getNotNullList(externalActivityList));
        batchActivity.addAll(getNotNullList(idleActivityList));
        batchActivity.addAll(getNotNullList(executionActivityList));
        batchActivity.addAll(getNotNullList(modificationActivityList));

        return batchActivity;
    }

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

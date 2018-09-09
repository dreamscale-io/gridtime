package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.activity.*;
import com.dreamscale.htmflow.api.batch.NewBatchEvent;
import com.dreamscale.htmflow.api.batch.NewIFMBatch;
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
public class FlowService {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    TimeService timeService;

    @Autowired
    FlowActivityRepository flowActivityRepository;

    @Autowired
    FlowEventRepository flowEventRepository;


    public void saveFlowBatch(UUID masterAccountId, NewIFMBatch batch) {
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(masterAccountId);

        Duration timeAdjustment = calculateTimeAdjustment(batch.getTimeSent());

        saveEditorActivity(memberEntity.getId(), timeAdjustment, batch.getEditorActivityList());
        saveExecutionActivity(memberEntity.getId(), timeAdjustment, batch.getExecutionActivityList());
        saveModificationActivity(memberEntity.getId(), timeAdjustment, batch.getModificationActivityList());
        saveIdleActivity(memberEntity.getId(), timeAdjustment, batch.getIdleActivityList());
        saveExternalActivity(memberEntity.getId(), timeAdjustment, batch.getExternalActivityList());

        saveEvents(memberEntity.getId(), timeAdjustment, batch.getEventList());
        saveSnippetEvents(memberEntity.getId(), timeAdjustment, batch.getSnippetEventList());

    }

    private void saveEvents(UUID memberId, Duration adjustment, List<NewBatchEvent> eventList) {
        for (NewBatchEvent event : eventList) {
            FlowEventEntity entity = new FlowEventEntity();

            entity.setEventType(event.getType());
            entity.setMemberId(memberId);
            entity.setTimePosition(event.getPosition().plus(adjustment));

            entity.setMetadataField("comment", event.getComment());

            flowEventRepository.save(entity);
        }
    }

    private void saveSnippetEvents(UUID memberId, Duration adjustment, List<NewSnippetEvent> snippetEvents) {
        for (NewSnippetEvent snippetEvent : snippetEvents) {
            FlowEventEntity entity = new FlowEventEntity();

            entity.setEventType(EventType.SNIPPET);
            entity.setMemberId(memberId);
            entity.setTimePosition(snippetEvent.getPosition().plus(adjustment));

            entity.setMetadataField("comment", snippetEvent.getComment());
            entity.setMetadataField("source", snippetEvent.getSource());
            entity.setMetadataField("snippet", snippetEvent.getSnippet());

            flowEventRepository.save(entity);
        }
    }

    private void saveIdleActivity(UUID memberId, Duration adjustment, List<NewIdleActivity> idleActivityList) {
        for (NewIdleActivity idleActivity : idleActivityList) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Idle);
            entity.setMemberId(memberId);
            entity.setStart(idleActivity.getEndTime());

            entity.setStart( idleActivity.getEndTime().plus(adjustment).minusSeconds(idleActivity.getDurationInSeconds()) );
            entity.setEnd( idleActivity.getEndTime().plus(adjustment) );

            flowActivityRepository.save(entity);
        }
    }

    private void saveExternalActivity(UUID memberId, Duration adjustment, List<NewExternalActivity> externalActivityList) {
        for (NewExternalActivity externalActivity : externalActivityList) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.External);
            entity.setMemberId(memberId);
            entity.setStart(externalActivity.getEndTime());

            entity.setStart( externalActivity.getEndTime().plus(adjustment).minusSeconds(externalActivity.getDurationInSeconds()) );
            entity.setEnd( externalActivity.getEndTime().plus(adjustment) );

            entity.setMetadataField("comment", externalActivity.getComment());

            flowActivityRepository.save(entity);
        }
    }

    private void saveModificationActivity(UUID memberId, Duration adjustment, List<NewModificationActivity> modificationActivityList) {
        for (NewModificationActivity modificationActivity : modificationActivityList) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Modification);
            entity.setMemberId(memberId);
            entity.setStart(modificationActivity.getEndTime());

            entity.setStart( modificationActivity.getEndTime().plus(adjustment).minusSeconds(modificationActivity.getDurationInSeconds()) );
            entity.setEnd( modificationActivity.getEndTime().plus(adjustment) );

            entity.setMetadataField("modificationCount", modificationActivity.getModificationCount());

            flowActivityRepository.save(entity);
        }
    }

    private void saveExecutionActivity(UUID memberId, Duration adjustment, List<NewExecutionActivity> executionActivityList) {
        for (NewExecutionActivity executionActivity : executionActivityList) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Execution);
            entity.setMemberId(memberId);
            entity.setStart(executionActivity.getEndTime());

            entity.setStart( executionActivity.getEndTime().plus(adjustment).minusSeconds(executionActivity.getDurationInSeconds()) );
            entity.setEnd( executionActivity.getEndTime().plus(adjustment) );

            entity.setMetadataField("processName", executionActivity.getProcessName());
            entity.setMetadataField("executionTaskType", executionActivity.getExecutionTaskType());
            entity.setMetadataField("exitCode", executionActivity.getExitCode());
            entity.setMetadataField("isDebug", executionActivity.isDebug());

            flowActivityRepository.save(entity);
        }
    }

    private void saveEditorActivity(UUID memberId, Duration adjustment, List<NewEditorActivity> editorActivityList) {

        for (NewEditorActivity editorActivity : editorActivityList) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Editor);
            entity.setMemberId(memberId);
            entity.setStart(editorActivity.getEndTime());

            entity.setStart( editorActivity.getEndTime().plus(adjustment).minusSeconds(editorActivity.getDurationInSeconds()) );
            entity.setEnd( editorActivity.getEndTime().plus(adjustment) );

            entity.setMetadataField("filePath", editorActivity.getFilePath());
            entity.setMetadataField("isModified", editorActivity.isModified());

            flowActivityRepository.save(entity);
        }
    }


    private Duration calculateTimeAdjustment(LocalDateTime messageSentAt) {
        LocalDateTime now = timeService.now();

        return Duration.between(messageSentAt, now);
    }

}

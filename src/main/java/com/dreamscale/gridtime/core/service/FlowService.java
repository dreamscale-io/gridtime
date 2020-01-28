package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchEventDto;
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import com.dreamscale.gridtime.api.flow.activity.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.flow.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    @Autowired
    RecentActivityService recentActivityService;

    //okay, next thing I need to do, is wire in a component resolve service, that will get used on batch processing
    //I can make the details more elaborate ovre time, but first, just call the function, and write a test that
    //validates the component is getting mapped

    public void saveFlowBatch(UUID masterAccountId, NewFlowBatchDto batch) {
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(masterAccountId);

        UUID mostRecentProjectId = lookupProjectIdOfMostRecentIntention(memberEntity);

        List<Activity> sortedBatchItems = sortAllItemsByTime(batch.getAllBatchActivity());
        Duration timeAdjustment = calculateTimeAdjustment(batch.getTimeSent());

        for (Activity activityInSequence : sortedBatchItems) {
            if (activityInSequence instanceof NewEditorActivityDto) {
                saveEditorActivity(memberEntity.getId(), timeAdjustment, (NewEditorActivityDto) activityInSequence);

            } else {
                saveActivity(memberEntity.getId(), timeAdjustment, activityInSequence);
            }
        }

        saveEvents(memberEntity.getId(), timeAdjustment, batch.getEventList());
    }

    private void saveActivity(UUID memberId, Duration timeAdjustment, Activity activity) {

        if (activity instanceof NewExecutionActivityDto) {
            saveExecutionActivity(memberId, timeAdjustment, (NewExecutionActivityDto) activity);
        } else if (activity instanceof NewModificationActivityDto) {
            saveModificationActivity(memberId, timeAdjustment, (NewModificationActivityDto) activity);
        } else if (activity instanceof NewIdleActivityDto) {
            saveIdleActivity(memberId, timeAdjustment, (NewIdleActivityDto) activity);
        } else if (activity instanceof NewExternalActivityDto) {
            saveExternalActivity(memberId, timeAdjustment, (NewExternalActivityDto) activity);
        }

    }

    //what is the most recent intention of the user that this batch corresponds to, the entire batch
    //will be processed with a single project context, based on whatever the last intention is
    //in general, devs shouldn't be switching between projects, but I will probably need to make this more robust
    //at some point, and somehow guess at the project from the file activity or something

    private UUID lookupProjectIdOfMostRecentIntention(OrganizationMemberEntity memberEntity) {
        return recentActivityService.lookupProjectIdOfMostRecentActivity(memberEntity);
    }

    private List<Activity> sortAllItemsByTime(List<Activity> allBatchActivity) {

        allBatchActivity.sort(new Comparator<Activity>() {
            @Override
            public int compare(Activity o1, Activity o2) {
                return o1.getEndTime().compareTo(o2.getEndTime());
            }
        });
        return allBatchActivity;
    }

    private void saveEvents(UUID memberId, Duration adjustment, List<NewFlowBatchEventDto> eventList) {
        for (NewFlowBatchEventDto event : eventList) {
            FlowEventEntity entity = new FlowEventEntity();

            entity.setEventType(FlowEventType.toFlowEventType(event.getType()));
            entity.setMemberId(memberId);
            entity.setTimePosition(event.getPosition().plus(adjustment));

            entity.setMetadataField(FlowEventMetadataField.comment, event.getComment());

            flowEventRepository.save(entity);
        }
    }

    public void saveSnippetEvent(UUID masterAccountId, NewSnippetEventDto snippetEvent) {
        // TOOD: this seems wrong... what is the 'default' membership and why are we getting it here?
        // shouldn't an api key be tied to a specific membership?  seems like a security hole
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(masterAccountId);

        FlowEventEntity entity = FlowEventEntity.builder()
                .eventType(FlowEventType.SNIPPET)
                .memberId(memberEntity.getId())
                .timePosition(snippetEvent.getPosition())
                .build();

        entity.setMetadataField(FlowEventMetadataField.filePath, snippetEvent.getFilePath());
        entity.setMetadataField(FlowEventMetadataField.lineNumber, snippetEvent.getLineNumber());
        entity.setMetadataField(FlowEventMetadataField.source, snippetEvent.getSource());
        entity.setMetadataField(FlowEventMetadataField.snippet, snippetEvent.getSnippet());

        flowEventRepository.save(entity);
    }

    private void saveIdleActivity(UUID memberId, Duration adjustment, NewIdleActivityDto idleActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Idle);
            entity.setMemberId(memberId);
            entity.setStart(idleActivity.getEndTime());

            entity.setStart(idleActivity.getEndTime().plus(adjustment).minusSeconds(idleActivity.getDurationInSeconds()));
            entity.setEnd(idleActivity.getEndTime().plus(adjustment));

            flowActivityRepository.save(entity);
    }

    private void saveExternalActivity(UUID memberId, Duration adjustment, NewExternalActivityDto externalActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.External);
            entity.setMemberId(memberId);
            entity.setStart(externalActivity.getEndTime());

            entity.setStart(externalActivity.getEndTime().plus(adjustment).minusSeconds(externalActivity.getDurationInSeconds()));
            entity.setEnd(externalActivity.getEndTime().plus(adjustment));

            entity.setMetadataField(FlowActivityMetadataField.comment, externalActivity.getComment());

            flowActivityRepository.save(entity);
    }

    private void saveModificationActivity(UUID memberId, Duration adjustment, NewModificationActivityDto modificationActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Modification);
            entity.setMemberId(memberId);
            entity.setStart(modificationActivity.getEndTime());

            entity.setStart(modificationActivity.getEndTime().plus(adjustment).minusSeconds(modificationActivity.getDurationInSeconds()));
            entity.setEnd(modificationActivity.getEndTime().plus(adjustment));

            entity.setMetadataField(FlowActivityMetadataField.modificationCount, modificationActivity.getModificationCount());

            flowActivityRepository.save(entity);
    }

    private void saveExecutionActivity(UUID memberId, Duration adjustment, NewExecutionActivityDto executionActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Execution);
            entity.setMemberId(memberId);
            entity.setStart(executionActivity.getEndTime());

            entity.setStart(executionActivity.getEndTime().plus(adjustment).minusSeconds(executionActivity.getDurationInSeconds()));
            entity.setEnd(executionActivity.getEndTime().plus(adjustment));

            entity.setMetadataField(FlowActivityMetadataField.processName, executionActivity.getProcessName());
            entity.setMetadataField(FlowActivityMetadataField.executionTaskType, executionActivity.getExecutionTaskType());
            entity.setMetadataField(FlowActivityMetadataField.exitCode, executionActivity.getExitCode());
            entity.setMetadataField(FlowActivityMetadataField.isDebug, executionActivity.isDebug());

            flowActivityRepository.save(entity);
    }

    private void saveEditorActivity(UUID memberId, Duration adjustment, NewEditorActivityDto editorActivity) {

        FlowActivityEntity entity = new FlowActivityEntity();

        entity.setActivityType(FlowActivityType.Editor);
        entity.setMemberId(memberId);
        entity.setStart(editorActivity.getEndTime());

        entity.setStart(editorActivity.getEndTime().plus(adjustment).minusSeconds(editorActivity.getDurationInSeconds()));
        entity.setEnd(editorActivity.getEndTime().plus(adjustment));

        entity.setMetadataField(FlowActivityMetadataField.filePath, editorActivity.getFilePath());
        entity.setMetadataField(FlowActivityMetadataField.isModified, editorActivity.isModified());

        flowActivityRepository.save(entity);
    }

    private Duration calculateTimeAdjustment(LocalDateTime messageSentAt) {
        LocalDateTime now = timeService.now();

        return Duration.between(messageSentAt, now);
    }

}

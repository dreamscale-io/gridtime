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
    ComponentLookupService componentLookupService;

    @Autowired
    RecentActivityService recentActivityService;

    //okay, next thing I need to do, is wire in a component lookup service, that will get used on batch processing
    //I can make the details more elaborate ovre time, but first, just call the function, and write a test that
    //validates the component is getting mapped

    public void saveFlowBatch(UUID masterAccountId, NewFlowBatch batch) {
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(masterAccountId);

        UUID mostRecentProjectId = lookupProjectIdOfMostRecentIntention(memberEntity);
        String mostRecentComponent = lookupComponentOfMostRecentActivity(memberEntity);

        List<Activity> sortedBatchItems = sortAllItemsByTime(batch.getAllBatchActivity());
        Duration timeAdjustment = calculateTimeAdjustment(batch.getTimeSent());

        for (Activity activityInSequence : sortedBatchItems) {
            if (activityInSequence instanceof NewEditorActivity) {
                String component = lookupComponent(mostRecentProjectId, (NewEditorActivity) activityInSequence);
                saveEditorActivity(memberEntity.getId(), component, timeAdjustment, (NewEditorActivity) activityInSequence);

                mostRecentComponent = component;
            } else {
                saveActivity(memberEntity.getId(), mostRecentComponent, timeAdjustment, activityInSequence);
            }
        }

        saveEvents(memberEntity.getId(), timeAdjustment, batch.getEventList());
    }

    private void saveActivity(UUID memberId,  String component, Duration timeAdjustment, Activity activity) {

        if (activity instanceof NewExecutionActivity) {
            saveExecutionActivity(memberId, component, timeAdjustment, (NewExecutionActivity) activity);
        } else if (activity instanceof NewModificationActivity) {
            saveModificationActivity(memberId, component, timeAdjustment, (NewModificationActivity) activity);
        } else if (activity instanceof NewIdleActivity) {
            saveIdleActivity(memberId, component, timeAdjustment, (NewIdleActivity) activity);
        } else if (activity instanceof NewExternalActivity) {
            saveExternalActivity(memberId, component, timeAdjustment, (NewExternalActivity) activity);
        }

    }

    //wire up component lookup service, that will use the configuration based on the project
    //and look up the component for that, caching all the things for that project, or returning a default

    private String lookupComponent(UUID projectId, NewEditorActivity editorActivity) {

        return componentLookupService.lookupComponent(projectId, editorActivity.getFilePath());
    }

    //query the most recent flow activity for a member feed, look at the component of the last batch,
    //and start from there.  Assumption of last batch happened about 30 min ago, so shouldn't have timing issues

    private String lookupComponentOfMostRecentActivity(OrganizationMemberEntity memberEntity) {

        return recentActivityService.lookupComponentOfMostRecentActivity(memberEntity);
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

    public void saveSnippetEvent(UUID masterAccountId, NewSnippetEvent snippetEvent) {
        // TOOD: this seems wrong... what is the 'default' membership and why are we getting it here?
        // shouldn't an api key be tied to a specific membership?  seems like a security hole
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(masterAccountId);

        FlowEventEntity entity = FlowEventEntity.builder()
                .eventType(EventType.SNIPPET)
                .memberId(memberEntity.getId())
                .timePosition(snippetEvent.getPosition())
                .build();

        entity.setMetadataField("comment", snippetEvent.getComment());
        entity.setMetadataField("source", snippetEvent.getSource());
        entity.setMetadataField("snippet", snippetEvent.getSnippet());

        flowEventRepository.save(entity);
    }

    private void saveIdleActivity(UUID memberId, String component, Duration adjustment, NewIdleActivity idleActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Idle);
            entity.setMemberId(memberId);
            entity.setStart(idleActivity.getEndTime());

            entity.setStart(idleActivity.getEndTime().plus(adjustment).minusSeconds(idleActivity.getDurationInSeconds()));
            entity.setEnd(idleActivity.getEndTime().plus(adjustment));

            entity.setComponent(component);
            flowActivityRepository.save(entity);
    }

    private void saveExternalActivity(UUID memberId, String component, Duration adjustment, NewExternalActivity externalActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.External);
            entity.setMemberId(memberId);
            entity.setStart(externalActivity.getEndTime());

            entity.setStart(externalActivity.getEndTime().plus(adjustment).minusSeconds(externalActivity.getDurationInSeconds()));
            entity.setEnd(externalActivity.getEndTime().plus(adjustment));

            entity.setMetadataField("comment", externalActivity.getComment());

            entity.setComponent(component);
            flowActivityRepository.save(entity);
    }

    private void saveModificationActivity(UUID memberId, String component, Duration adjustment, NewModificationActivity modificationActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Modification);
            entity.setMemberId(memberId);
            entity.setStart(modificationActivity.getEndTime());

            entity.setStart(modificationActivity.getEndTime().plus(adjustment).minusSeconds(modificationActivity.getDurationInSeconds()));
            entity.setEnd(modificationActivity.getEndTime().plus(adjustment));

            entity.setMetadataField("modificationCount", modificationActivity.getModificationCount());

            entity.setComponent(component);
            flowActivityRepository.save(entity);
    }

    private void saveExecutionActivity(UUID memberId, String component, Duration adjustment, NewExecutionActivity executionActivity) {
            FlowActivityEntity entity = new FlowActivityEntity();

            entity.setActivityType(FlowActivityType.Execution);
            entity.setMemberId(memberId);
            entity.setStart(executionActivity.getEndTime());

            entity.setStart(executionActivity.getEndTime().plus(adjustment).minusSeconds(executionActivity.getDurationInSeconds()));
            entity.setEnd(executionActivity.getEndTime().plus(adjustment));

            entity.setMetadataField("processName", executionActivity.getProcessName());
            entity.setMetadataField("executionTaskType", executionActivity.getExecutionTaskType());
            entity.setMetadataField("exitCode", executionActivity.getExitCode());
            entity.setMetadataField("isDebug", executionActivity.isDebug());

            entity.setComponent(component);

            flowActivityRepository.save(entity);
    }

    private String saveEditorActivity(UUID memberId, String component, Duration adjustment, NewEditorActivity editorActivity) {

        FlowActivityEntity entity = new FlowActivityEntity();

        entity.setActivityType(FlowActivityType.Editor);
        entity.setMemberId(memberId);
        entity.setStart(editorActivity.getEndTime());

        entity.setStart(editorActivity.getEndTime().plus(adjustment).minusSeconds(editorActivity.getDurationInSeconds()));
        entity.setEnd(editorActivity.getEndTime().plus(adjustment));

        entity.setMetadataField("filePath", editorActivity.getFilePath());
        entity.setMetadataField("isModified", editorActivity.isModified());

        entity.setComponent(component);

        flowActivityRepository.save(entity);

        return component;
    }

    private Duration calculateTimeAdjustment(LocalDateTime messageSentAt) {
        LocalDateTime now = timeService.now();

        return Duration.between(messageSentAt, now);
    }

}

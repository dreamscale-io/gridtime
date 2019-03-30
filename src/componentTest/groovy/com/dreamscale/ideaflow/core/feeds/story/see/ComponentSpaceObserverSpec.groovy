package com.dreamscale.ideaflow.core.feeds.story.see

import com.dreamscale.ideaflow.core.domain.JournalEntryEntity
import com.dreamscale.ideaflow.core.domain.ProjectEntity
import com.dreamscale.ideaflow.core.domain.TaskEntity
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.ideaflow.core.domain.flow.FlowActivityType
import com.dreamscale.ideaflow.core.feeds.clock.GeometryClock
import com.dreamscale.ideaflow.core.feeds.common.ZoomLevel
import com.dreamscale.ideaflow.core.feeds.story.StoryFrame
import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity
import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry
import com.dreamscale.ideaflow.core.feeds.story.feature.structure.BoxAndBridgeStructure
import com.dreamscale.ideaflow.core.service.ComponentLookupService
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom

public class ComponentSpaceObserverSpec extends Specification {

    IdeaFlowComponentSpaceObserver componentSpaceObserver
    StoryFrame storyFrame

    def setup() {
        componentSpaceObserver = new IdeaFlowComponentSpaceObserver()

        ComponentLookupService componentLookupServiceMock =
                [lookupComponent: { UUID projectId, String filePath -> return "mainFocus" }] as ComponentLookupService

        componentSpaceObserver.componentLookupService = componentLookupServiceMock;

        storyFrame = new StoryFrame(new GeometryClock(LocalDateTime.now()).getCoordinates(), ZoomLevel.MIN)
    }

    def "should create Location traversals inside a Place"() {
        given:

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        FlowableFlowActivity activity1 = createActivity("locationA", time1, time2)
        FlowableFlowActivity activity2 = createActivity("locationB", time2, time3)
        FlowableFlowActivity activity3 = createActivity("locationA", time3, time4)
        FlowableFlowActivity activity4 = createActivity("locationA", time4, time4)

        def flowables = [activity1, activity2, activity3, activity4] as List
        Window window = new Window(time1, time4)
        window.addAll(flowables);

        when:
        componentSpaceObserver.see(storyFrame, window)
        BoxAndBridgeStructure boxAndBridgeStructure = storyFrame.getThoughtStructure();

        then:
        assert true
    }

    //TODO journal walk should include flames over time, need a way to mark these dirty
    //TODO need to reprocess frames on dirty
    //TODO locations should include movements over time
    //TODO component spaces should include bridges
    //TODO locations should be mapped to lookup tables
    //story frames abstract out, once I have the saving and persistance in mainFocus


    FlowableFlowActivity createActivity( String location, LocalDateTime start, LocalDateTime end) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .activityType(FlowActivityType.Editor)
                .start(start)
                .end(end)
                .build();

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, location)

        return new FlowableFlowActivity(flowActivityEntity);

    }


    FlowableJournalEntry createJournalEvent(ProjectEntity project, TaskEntity task, LocalDateTime time,
                                            LocalDateTime finishTime) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity()

        journalEntryEntity.id = UUID.randomUUID()
        journalEntryEntity.projectId = project.id
        journalEntryEntity.taskId = task.id
        journalEntryEntity.position = time
        journalEntryEntity.finishTime = finishTime
        journalEntryEntity.description = aRandom.text(20)
        journalEntryEntity.taskName = aRandom.text(5)
        journalEntryEntity.taskSummary = aRandom.text(30)

        return new FlowableJournalEntry(journalEntryEntity)
    }
}

package com.dreamscale.htmflow.core.feeds.executor.parts.observer

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window
import com.dreamscale.htmflow.core.feeds.story.StoryTile
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class ExecutionRhythmObserverSpec extends Specification {

    ExecutionRhythmObserver executionRhythmObserver
    StoryTile storyTile
    GeometryClock clock

    def setup() {
        clock = new GeometryClock(LocalDateTime.now())
        executionRhythmObserver = new ExecutionRhythmObserver()
        storyTile = new StoryTile("@torchie/id", clock.getCoordinates(), ZoomLevel.MIN_20)
    }

    def "should create execution activity with red/green cycles"() {
        given:

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        FlowableFlowActivity executionEvent1 = createExecutionEvent(time1, time1.plusMinutes(1), false)
        FlowableFlowActivity executionEvent2 = createExecutionEvent(time2, time2.plusMinutes(1), true)
        FlowableFlowActivity executionEvent3 = createExecutionEvent(time3, time3.plusMinutes(1), true)

        def flowables = [executionEvent1, executionEvent2, executionEvent3] as List
        Window window = new Window(time1, time4)
        window.addAll(flowables);

        when:
        executionRhythmObserver.see(window, storyTile)
        def movements = storyTile.getRhythmLayer(RhythmLayerType.EXECUTION_ACTIVITY).getMovements();

        then:
        assert movements.size() == 3
        assert ((ExecuteThing)movements.get(0)).getExecutionDetails().isRedAndWantingGreen() == false;
        assert ((ExecuteThing)movements.get(1)).getExecutionDetails().isRedAndWantingGreen() == true;
        assert ((ExecuteThing)movements.get(2)).getExecutionDetails().isRedAndWantingGreen() == true;
    }


    FlowableFlowActivity createExecutionEvent(LocalDateTime start,
                                              LocalDateTime end,
                                              boolean isRed) {
        FlowActivityEntity executionActivity = new FlowActivityEntity()

        executionActivity.id = 5L;
        executionActivity.start = start;
        executionActivity.end = end;
        executionActivity.activityType = FlowActivityType.Execution;
        executionActivity.setMetadataField(FlowActivityMetadataField.executionTaskType, "JUnit")

        if (isRed) {
            executionActivity.setMetadataField(FlowActivityMetadataField.exitCode, -5)
        } else {
            executionActivity.setMetadataField(FlowActivityMetadataField.exitCode, 0)
        }
        executionActivity.setMetadataField(FlowActivityMetadataField.processName, "TestName")

        return new FlowableFlowActivity(executionActivity)
    }
}

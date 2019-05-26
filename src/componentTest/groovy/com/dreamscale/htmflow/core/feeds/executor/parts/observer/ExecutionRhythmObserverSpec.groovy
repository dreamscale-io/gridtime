package com.dreamscale.htmflow.core.feeds.executor.parts.observer

import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableFlowActivity
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window
import com.dreamscale.htmflow.core.feeds.story.TileBuilder
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class ExecutionRhythmObserverSpec extends Specification {

    ExecutionRhythmObserver executionRhythmObserver
    TileBuilder storyTile
    GeometryClock clock

    def setup() {
        clock = new GeometryClock(LocalDateTime.now())
        executionRhythmObserver = new ExecutionRhythmObserver()
        storyTile = new TileBuilder("@torchie/id", clock.getCoordinates(), ZoomLevel.TWENTIES)
    }

    def "should create red/green cycles from execution activity"() {
        given:

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(3);
        LocalDateTime time3 = time2.plusMinutes(4);
        LocalDateTime time4 = time3.plusMinutes(5);

        FlowableFlowActivity executionEvent1 = createTestExecutionEvent(time1, time1.plusSeconds(1), false)
        FlowableFlowActivity executionEvent2 = createTestExecutionEvent(time2, time2.plusSeconds(2), true)
        FlowableFlowActivity executionEvent3 = createOtherExecutionEvent(time3, time3.plusSeconds(1))
        FlowableFlowActivity executionEvent4 = createTestExecutionEvent(time4, time4.plusSeconds(2), false)

        def flowables = [executionEvent1, executionEvent2, executionEvent3, executionEvent4] as List
        Window window = new Window(time1, time4)
        window.addAll(flowables);

        when:
        executionRhythmObserver.seeInto(window, storyTile)
        def movements = storyTile.getRhythmLayer(RhythmLayerType.EXECUTION_ACTIVITY).getMovements();

        then:
        assert movements.size() == 4
        assert ((ExecuteThing)movements.get(0)).getExecutionDetails().isRedAndWantingGreen() == false;
        assert ((ExecuteThing)movements.get(1)).getExecutionDetails().isRedAndWantingGreen() == true;
        assert ((ExecuteThing)movements.get(2)).getExecutionDetails().isRedAndWantingGreen() == true;
        assert ((ExecuteThing)movements.get(3)).getExecutionDetails().isRedAndWantingGreen() == false;
    }


    FlowableFlowActivity createOtherExecutionEvent(LocalDateTime start,
                                                  LocalDateTime end) {
        FlowActivityEntity executionActivity = new FlowActivityEntity()

        executionActivity.id = 5L;
        executionActivity.start = start;
        executionActivity.end = end;
        executionActivity.activityType = FlowActivityType.Execution;
        executionActivity.setMetadataField(FlowActivityMetadataField.executionTaskType, "Application")
        executionActivity.setMetadataField(FlowActivityMetadataField.exitCode, 0)
        executionActivity.setMetadataField(FlowActivityMetadataField.processName, "Hello")

        return new FlowableFlowActivity(executionActivity)
    }

    FlowableFlowActivity createTestExecutionEvent(LocalDateTime start,
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

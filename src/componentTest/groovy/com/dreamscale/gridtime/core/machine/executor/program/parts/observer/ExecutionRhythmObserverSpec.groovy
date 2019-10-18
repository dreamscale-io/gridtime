package com.dreamscale.gridtime.core.machine.executor.program.parts.observer

import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.gridtime.core.domain.flow.FlowActivityType
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

public class ExecutionRhythmObserverSpec extends Specification {

    ExecutionRhythmObserver executionRhythmObserver
    GeometryClock clock
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    GridTile gridTile
    UUID torchieId

    def setup() {

        clock = new GeometryClock(aRandom.localDateTime())

        time1 = clock.getActiveGridTime().getClockTime()
        time2 = time1.plusMinutes(3);
        time3 = time2.plusMinutes(4);
        time4 = time3.plusMinutes(3);


        executionRhythmObserver = new ExecutionRhythmObserver()

        torchieId = UUID.randomUUID();

        TeamBoxConfiguration teamBoxConfiguration = new TeamBoxConfiguration.Builder().build();

        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), new FeatureCache(), teamBoxConfiguration);
    }

    def "should create red/green cycles from execution activity"() {
        given:
        FlowableFlowActivity executionEvent1 = createTestExecutionEvent(time1, time1.plusSeconds(1), false)
        FlowableFlowActivity executionEvent2 = createTestExecutionEvent(time2, time2.plusSeconds(2), true)
        FlowableFlowActivity executionEvent3 = createOtherExecutionEvent(time3, time3.plusSeconds(3))
        FlowableFlowActivity executionEvent4 = createTestExecutionEvent(time4, time4.plusSeconds(4), false)

        def flowables = [executionEvent1, executionEvent2, executionEvent3, executionEvent4] as List
        def window = new Window(time1, time1.plusMinutes(20));
        window.addAll(flowables)

        when:
        executionRhythmObserver.see(window, gridTile)
        MusicGridResults results = gridTile.playTrack(TrackSetKey.Executions);
        println results

        then:
        assert results.getCell("@exec/rhythm", "20.1") == "g"
        assert results.getCell("@exec/rhythm", "20.4") == "r^"
        assert results.getCell("@exec/rhythm", "20.8") == "a"
        assert results.getCell("@exec/rhythm", "20.11") == "g\$"

        assert results.getCell("@exec/cycletim", "20.1") == ""
        assert results.getCell("@exec/cycletim", "20.4") == "180.0"
        assert results.getCell("@exec/cycletim", "20.8") == "240.0"
        assert results.getCell("@exec/cycletim", "20.11") == "180.0"

    }


    FlowableFlowActivity createOtherExecutionEvent(LocalDateTime start,
                                                  LocalDateTime end) {
        FlowActivityEntity executionActivity = new FlowActivityEntity()

        executionActivity.id = aRandom.nextLong();
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

        executionActivity.id = aRandom.nextLong();
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

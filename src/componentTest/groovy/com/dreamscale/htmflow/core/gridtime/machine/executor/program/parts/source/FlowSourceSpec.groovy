package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.journal.IntentionEntity
import com.dreamscale.htmflow.core.domain.journal.ProjectEntity
import com.dreamscale.htmflow.core.domain.journal.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.FileActivityFetcher
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.JournalFetcher
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserver
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyFeaturePool
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class FlowSourceSpec extends Specification {

    @Autowired
    JournalFetcher journalFetcher

   @Autowired
   FileActivityFetcher fileActivityFetcher

    UUID memberId
    FeaturePool featurePool
    FlowSource journalFlowSource
    Window latestWindow
    FlowSource activityFlowSource

    def setup() {
        this.memberId = UUID.randomUUID();
        this.featurePool = new MemoryOnlyFeaturePool(memberId);

        FlowObserver flowObserverMock =
                [see: { Window window, FeaturePool pool -> this.latestWindow = window }] as FlowObserver

        this.journalFlowSource = new FlowSource(memberId, featurePool, journalFetcher, flowObserverMock)
        this.activityFlowSource = new FlowSource(memberId, featurePool, fileActivityFetcher, flowObserverMock)

    }

    def "should create window with inclusive start and exclusive end"() {
        given:
        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);

        createEvent(memberId, time1)
        createEvent(memberId, time2)
        createEvent(memberId, time3)

        when:
        journalFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time1, time3))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 2
        assert latestWindow.flowables.get(0).get().position == time1
        assert latestWindow.flowables.get(1).get().position == time2

    }

    def "should break up events across windows"() {
        given:
        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time2_5 = time2_0.plusMinutes(10);
        LocalDateTime time3_0 = time2_0.plusMinutes(20);


        createEvent(memberId, time1_0)
        createEvent(memberId, time2_0)
        createEvent(memberId, time2_5)

        when:
        journalFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time1_0, time2_0))
        journalFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time2_0, time3_0))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 2
        assert latestWindow.flowables.get(0).get().position == time2_0
        assert latestWindow.flowables.get(1).get().position == time2_5

    }

    def "should return empty windows when no events inside"() {
        given:
        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time2_5 = time2_0.plusMinutes(10);
        LocalDateTime time3_0 = time2_0.plusMinutes(20);


        createEvent(memberId, time2_0)
        createEvent(memberId, time2_5)

        when:
        journalFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time1_0, time2_0))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 0
        assert latestWindow.isFinished() == true
    }

    def "should return open windows when no events after window"() {
        given:
        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time2_5 = time2_0.plusMinutes(10);
        LocalDateTime time3_0 = time2_0.plusMinutes(20);


        createEvent(memberId, time2_0)
        createEvent(memberId, time2_5)

        when:
        journalFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time2_0, time3_0))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 2
        assert latestWindow.isFinished() == false
    }

    def "should trim activity to fit in window"() {
        given:
        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time3_0 = time2_0.plusMinutes(20);
        LocalDateTime time4_0 = time3_0.plusMinutes(20);

        createActivity(memberId, time1_0, time4_0)

        when:
        activityFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY,time1_0, time2_0))
        activityFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time2_0, time3_0))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 1
        assert latestWindow.flowables.get(0).get().start == time2_0
        assert latestWindow.flowables.get(0).get().end == time3_0

    }

    def "should include activity with ending overlap in window"() {
        given:
        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time2_5 = time2_0.plusMinutes(5);
        LocalDateTime time3_0 = time2_0.plusMinutes(20);
        LocalDateTime time4_0 = time3_0.plusMinutes(20);

        createActivity(memberId, time1_0, time2_5)

        when:
        activityFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY,time1_0, time2_0))
        activityFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY,time2_0, time3_0))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 1
        assert latestWindow.flowables.get(0).get().start == time2_0
        assert latestWindow.flowables.get(0).get().end == time2_5

    }

    def "should include activity with beginning overlap in window"() {
        given:
        LocalDateTime time1_0 = aRandom.localDateTime()
        LocalDateTime time2_0 = time1_0.plusMinutes(20);
        LocalDateTime time2_5 = time2_0.plusMinutes(5);
        LocalDateTime time3_0 = time2_0.plusMinutes(20);
        LocalDateTime time4_0 = time3_0.plusMinutes(20);

        createActivity(memberId, time2_5, time4_0)

        when:
        activityFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time1_0, time2_0))
        activityFlowSource.tick(Metronome.createTick(ZoomLevel.TWENTY, time2_0, time3_0))

        then:
        assert latestWindow != null;
        assert latestWindow.flowables.size() == 1
        assert latestWindow.flowables.get(0).get().start == time2_5
        assert latestWindow.flowables.get(0).get().end == time3_0

    }




    void createEvent(UUID memberId, LocalDateTime time) {
        ProjectEntity projectEntity = aRandom.projectEntity().save();
        TaskEntity taskEntity = aRandom.taskEntity().forProject(projectEntity).save();

        IntentionEntity journalEntry = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

    }

    void createActivity(UUID memberId, LocalDateTime start, LocalDateTime end) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(end)
                .save()

    }


}

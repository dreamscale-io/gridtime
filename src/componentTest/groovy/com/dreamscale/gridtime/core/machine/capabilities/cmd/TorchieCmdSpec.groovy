package com.dreamscale.gridtime.core.machine.capabilities.cmd


import com.dreamscale.gridtime.core.domain.member.json.Member
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.GridTimeExecutor
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results
import com.dreamscale.gridtime.core.machine.executor.worker.DefaultWorkPile
import com.dreamscale.gridtime.core.machine.memory.tag.types.FinishTypeTag
import com.dreamscale.gridtime.core.machine.memory.tag.types.StartTypeTag
import com.dreamscale.gridtime.core.machine.executor.program.NoOpProgram
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections
import com.dreamscale.gridtime.core.machine.memory.TorchieState
import com.dreamscale.gridtime.core.machine.memory.MemoryOnlyTorchieState
import com.dreamscale.gridtime.core.machine.memory.feature.details.AuthorsDetails
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircuitDetails
import com.dreamscale.gridtime.core.machine.memory.feature.details.ExecutionEvent
import com.dreamscale.gridtime.core.machine.memory.feature.details.WorkContextEvent
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

class TorchieCmdSpec extends Specification {

    UUID torchieId
    TorchieState torchieState
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    TorchieCmd cmd

    Torchie torchie
    LocalDateTime clockStart

    GridTimeExecutor gridTimeExecutor


    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(2)
        time3 = clockStart.plusMinutes(3)
        time4 = clockStart.plusMinutes(6)

        torchieId = UUID.randomUUID();
        torchieState = new MemoryOnlyTorchieState(torchieId);

        torchie = new Torchie(torchieId, torchieState, new NoOpProgram());
        System.out.println(clockStart);

        DefaultWorkPile workerPool = new DefaultWorkPile()
        gridTimeExecutor = new GridTimeExecutor(workerPool);
        gridTimeExecutor.start()

        cmd = new TorchieCmd(workerPool, torchie);
        cmd.haltProgram()
       
    }

    def "goto specific tile by timestamp"() {
        when:

        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        then:
        assert torchieState.getActiveTile() != null
        assert torchieState.getActiveTile().gridTime.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+2:20"

    }

    def "goto tile and play track"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        torchieState.getActiveTile().startWTF(time3, new CircuitDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)

        torchieState.getActiveTile().finishAfterLoad()

        when:
        MusicGridResults trackOutput = cmd.playTrack(TrackSetKey.IdeaFlow)
        print trackOutput;

        then:
        assert trackOutput != null
        assert trackOutput.getCell("@flow/wtf", "20.4") == "wtf^"
    }

    def "goto tile and play execution track with red/green"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        torchieState.getActiveTile().executeThing(new ExecutionEvent(1, time1, Duration.ofSeconds(15), "JUnit", 0))
        torchieState.getActiveTile().executeThing(new ExecutionEvent(2, time1, Duration.ofSeconds(1), "JUnit", -1))
        torchieState.getActiveTile().executeThing(new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))
        torchieState.getActiveTile().executeThing(new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))

        torchieState.getActiveTile().executeThing(new ExecutionEvent(4, time2, Duration.ofSeconds(1), "JUnit", 0))
        torchieState.getActiveTile().executeThing(new ExecutionEvent(5, time3, Duration.ofSeconds(7), "JUnit", 0))

        torchieState.getActiveTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetKey.Executions)
        print trackOutput;

        then:
        assert trackOutput != null
        assert trackOutput.getCell("@exec/rhythm", "20.2") == "gr^rr"
        assert trackOutput.getCell("@exec/rhythm", "20.3") == "g\$"
        assert trackOutput.getCell("@exec/runtime", "20.3") == "1.0"
        assert trackOutput.getCell("@exec/cycletim", "20.3") == "60.0"
    }

    def "goto tile and play navigation rhythm track"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        torchieState.getActiveTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(6))
        torchieState.getActiveTile().gotoLocation(time1, "/some/other/place", Duration.ofSeconds(9))
        torchieState.getActiveTile().gotoLocation(time1, "/some/placeB", Duration.ofSeconds(12))
        torchieState.getActiveTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(5))

        torchieState.getActiveTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(9))
        torchieState.getActiveTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(12))

        torchieState.getActiveTile().modifyCurrentLocation(time2, 443)
        torchieState.getActiveTile().modifyCurrentLocation(time2, 243)

        torchieState.getActiveTile().gotoLocation(time3, "/some/other/place", Duration.ofSeconds(9))
        torchieState.getActiveTile().gotoLocation(time3, "/some/placeB", Duration.ofSeconds(12))

        torchieState.getActiveTile().finishAfterLoad()

        when:
        MusicGridResults trackOutput = cmd.playTrack(TrackSetKey.Navigations)
        print trackOutput;

        then:
        assert trackOutput != null
        assert trackOutput.getCell("@nav/rhythm", "20.2") == "abca"
        assert trackOutput.getCell("@nav/rhythm", "20.3") == "cc"
        assert trackOutput.getCell("@nav/rhythm", "20.4") == "bc"

        assert trackOutput.getCell("@nav/batch", "20.2") == "abc"
    }

    def "change work contexts and update bands and start/finish tags"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        UUID projA =  UUID.randomUUID();
        UUID taskA = UUID.randomUUID();
        UUID intentionA = UUID.randomUUID();
        UUID taskB = UUID.randomUUID();
        UUID intentionB = UUID.randomUUID();


        torchieState.getActiveTile().startWorkContext(time1, createWorkContextEvent("projectA", "taskA", "intA"));
        torchieState.getActiveTile().startWorkContext(time3, createWorkContextEvent("projectA", "taskB", "intB"));

        torchieState.getActiveTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetKey.WorkContext)
        print trackOutput;

        then:
        assert trackOutput != null

        assert trackOutput.getCell("@work/project", "20.2") == "proje*^"
        assert trackOutput.getCell("@work/task", "20.2") == "taskA^"
        assert trackOutput.getCell("@work/task", "20.3") == "taskA\$"
        assert trackOutput.getCell("@work/task", "20.4") == "taskB^"

    }

    def "authors print initials in track"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        Member arty = new Member(UUID.randomUUID().toString(), "Arty Starr");
        Member mike = new Member(UUID.randomUUID().toString(), "Mike Lueders");

        torchieState.getActiveTile().startAuthors(time1, new AuthorsDetails(DefaultCollections.toList(arty, mike)));
        torchieState.getActiveTile().startAuthors(time3, new AuthorsDetails(DefaultCollections.toList(arty)));

        torchieState.getActiveTile().clearAuthors(time4)

        torchieState.getActiveTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetKey.Authors)
        print trackOutput;

        then:
        assert trackOutput != null
        assert trackOutput.getCell("@author", "20.2") == "AS ML^"
        assert trackOutput.getCell("@author", "20.3") == "AS ML\$"
        assert trackOutput.getCell("@author", "20.4") == "AS^"
        assert trackOutput.getCell("@author", "20.7") == "AS\$"
        assert trackOutput.getCell("@author", "20.8") == ""

    }

    def "carry over context from one tile to the next"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        Member arty = new Member(UUID.randomUUID().toString(), "Arty Starr");

        torchieState.getActiveTile().startWorkContext(time3, createWorkContextEvent("projA", "taskA", "intA"));
        torchieState.getActiveTile().startAuthors(time1, new AuthorsDetails(DefaultCollections.toList(arty)));

        torchieState.getActiveTile().startFeelsBand(time2.plusMinutes(5), -4)
        torchieState.getActiveTile().startWTF(time3, new CircuitDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)

        torchieState.getActiveTile().gotoLocation(time2, "/some/placeA", Duration.ofSeconds(9))
        torchieState.getActiveTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(12))

        torchieState.getActiveTile().executeThing(new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))
        torchieState.getActiveTile().executeThing(new ExecutionEvent(4, time2, Duration.ofSeconds(5), "JUnit", -3))

        torchieState.getActiveTile().modifyCurrentLocation(time2, 443)

        torchieState.getActiveTile().finishAfterLoad()

        when:
        MusicGridResults firstTile = cmd.playTile()
        print firstTile;

        cmd.nextTile();

        torchieState.getActiveTile().executeThing(
                new ExecutionEvent(5, time2.plusMinutes(20), Duration.ofSeconds(5), "JUnit", -2))
        torchieState.getActiveTile().finishAfterLoad()

        MusicGridResults secondTile = cmd.playTile()
        print secondTile;

        then:
        assert firstTile != null;
        assert secondTile != null;

        assert firstTile.getCell("@flow/wtf", "20.4") == "wtf^"
        assert secondTile.getCell("@flow/wtf", "20.4") == "wtf"
        assert secondTile.getCell("@exec/cycletim", "20.3") == "1200.0"
    }

    def "should create progress band when modification threshold reached"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        torchieState.getActiveTile().startWTF(time1, new CircuitDetails(UUID.randomUUID(), "hi"), StartTypeTag.Resume)
        torchieState.getActiveTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(9))

        torchieState.getActiveTile().modifyCurrentLocation(time1, 100)
        torchieState.getActiveTile().modifyCurrentLocation(time2, 443)

        torchieState.getActiveTile().modifyCurrentLocation(time4, 20)

        torchieState.getActiveTile().finishAfterLoad()

        when:
        MusicGridResults firstTile = cmd.playTile()
        print firstTile;

        cmd.nextTile();

        torchieState.getActiveTile().clearWTF(time3.plusMinutes(20), FinishTypeTag.DoItLater)
        torchieState.getActiveTile().finishAfterLoad()

        MusicGridResults secondTile = cmd.playTile()
        print secondTile

        then:
        assert firstTile != null;
        assert secondTile != null;

        assert firstTile.getCell("@flow/learning", "20.1") == "prg^"

        assert firstTile.getQuarterCell("@flow/modify", "20.1") == "543.0"
        assert firstTile.getQuarterCell("@flow/modify", "20.6") == "563.0"
        assert firstTile.getQuarterCell("@flow/modify", "20.11") == "563.0"
        assert firstTile.getQuarterCell("@flow/modify", "20.16") == "563.0"

        assert secondTile.getCell("@flow/learning", "20.1") == "prg"
        assert secondTile.getCell("@flow/learning", "20.6") == "lrn^"
        assert secondTile.getCell("@flow/learning", "20.20") == "lrn"

        assert secondTile.getQuarterCell("@flow/modify", "20.1") == "563.0"
        assert secondTile.getQuarterCell("@flow/modify", "20.6") == "20.0"
        assert secondTile.getQuarterCell("@flow/modify", "20.11") == "0.0"
        assert secondTile.getQuarterCell("@flow/modify", "20.16") == "0.0"

    }

    private WorkContextEvent createWorkContextEvent(String projectName, String taskName, String intentionDescription) {
        return new WorkContextEvent(UUID.randomUUID(), intentionDescription, UUID.randomUUID(), taskName, UUID.randomUUID(), projectName)
    }


    //so the next things to do...

    //feature resolver, add support for boxes & bridges
    //save matrix grid

    //integration harness

    //aggregate up
    //traversals with bridges
    //timestamp positions on start/end tags
    //matrix analytics by project, task, component

    //batch box track?
    //traversal track?

    //analytics per box




//    def "play track SHOULD return text-console out for printing the specified track row"() {
//        given:
//
//
//
//        String cmdStr = "play /track/set/WorkContext"
//
//        when:
//        Results results = cmd.runSyncCommand(CmdType.PLAY_TRACK, cmdStr);
//
//        then:
//        assert results != null
//    }
}

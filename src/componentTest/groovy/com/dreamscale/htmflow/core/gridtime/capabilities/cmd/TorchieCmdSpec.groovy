package com.dreamscale.htmflow.core.gridtime.capabilities.cmd

import com.dreamscale.htmflow.core.domain.member.json.Member
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.machine.TorchiePoolExecutor
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.types.FinishTypeTag
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.types.StartTypeTag
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.NoOpJob
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyFeaturePool
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.AuthorsDetails
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.CircleDetails
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.ExecutionEvent
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.WorkContextEvent
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track.TrackSetName
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

class TorchieCmdSpec extends Specification {

    UUID torchieId
    FeaturePool featurePool
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    TorchieCmd cmd
    TorchiePoolExecutor torchieExecutor
    Torchie torchie
    LocalDateTime clockStart

    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(2)
        time3 = clockStart.plusMinutes(3)
        time4 = clockStart.plusMinutes(6)

        torchieId = UUID.randomUUID();
        featurePool = new MemoryOnlyFeaturePool(torchieId);

        torchie = new Torchie(torchieId, featurePool, new NoOpJob(featurePool));
        System.out.println(clockStart);
        
        torchieExecutor = new TorchiePoolExecutor(1);

        cmd = new TorchieCmd(torchieExecutor, torchie);
        cmd.haltMetronome()
       
    }

    def "goto specific tile by timestamp"() {
        when:

        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        then:
        assert featurePool.getActiveGridTile() != null
        assert featurePool.getActiveGridTile().gridCoordinates.getFormattedGridTime() == "2019-B1-W1-D1_12am+2:20"

    }

    def "goto tile and play track"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        featurePool.getActiveGridTile().startWTF(time3, new CircleDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        MusicGridResults trackOutput = cmd.playTrack(TrackSetName.IdeaFlow)
        print trackOutput;

        then:
        assert trackOutput != null
        assert trackOutput.getCell("@flow/wtf", "20.4") == "wtf^"
    }

    def "goto tile and play execution track with red/green"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        featurePool.getActiveGridTile().executeThing(time1, new ExecutionEvent(1, time1, Duration.ofSeconds(15), "JUnit", 0))
        featurePool.getActiveGridTile().executeThing(time1, new ExecutionEvent(2, time1, Duration.ofSeconds(1), "JUnit", -1))
        featurePool.getActiveGridTile().executeThing(time1, new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))
        featurePool.getActiveGridTile().executeThing(time1, new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))

        featurePool.getActiveGridTile().executeThing(time2, new ExecutionEvent(4, time2, Duration.ofSeconds(1), "JUnit", 0))
        featurePool.getActiveGridTile().executeThing(time3, new ExecutionEvent(5, time3, Duration.ofSeconds(7), "JUnit", 0))

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetName.Executions)
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

        featurePool.getActiveGridTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(6))
        featurePool.getActiveGridTile().gotoLocation(time1, "/some/other/place", Duration.ofSeconds(9))
        featurePool.getActiveGridTile().gotoLocation(time1, "/some/placeB", Duration.ofSeconds(12))
        featurePool.getActiveGridTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(5))

        featurePool.getActiveGridTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(9))
        featurePool.getActiveGridTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(12))

        featurePool.getActiveGridTile().modifyCurrentLocation(time2, 443)
        featurePool.getActiveGridTile().modifyCurrentLocation(time2, 243)

        featurePool.getActiveGridTile().gotoLocation(time3, "/some/other/place", Duration.ofSeconds(9))
        featurePool.getActiveGridTile().gotoLocation(time3, "/some/placeB", Duration.ofSeconds(12))

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        MusicGridResults trackOutput = cmd.playTrack(TrackSetName.Navigations)
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


        featurePool.getActiveGridTile().startWorkContext(time1, createWorkContextEvent("projectA", "taskA", "intA"));
        featurePool.getActiveGridTile().startWorkContext(time3, createWorkContextEvent("projectA", "taskB", "intB"));

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetName.WorkContext)
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

        featurePool.getActiveGridTile().startAuthors(time1, new AuthorsDetails(DefaultCollections.toList(arty, mike)));
        featurePool.getActiveGridTile().startAuthors(time3, new AuthorsDetails(DefaultCollections.toList(arty)));

        featurePool.getActiveGridTile().clearAuthors(time4)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetName.Authors)
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

        featurePool.getActiveGridTile().startWorkContext(time3, createWorkContextEvent("projA", "taskA", "intA"));
        featurePool.getActiveGridTile().startAuthors(time1, new AuthorsDetails(DefaultCollections.toList(arty)));

        featurePool.getActiveGridTile().startFeelsBand(time2.plusMinutes(5), -4)
        featurePool.getActiveGridTile().startWTF(time3, new CircleDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)

        featurePool.getActiveGridTile().gotoLocation(time2, "/some/placeA", Duration.ofSeconds(9))
        featurePool.getActiveGridTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(12))

        featurePool.getActiveGridTile().executeThing(time1, new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))
        featurePool.getActiveGridTile().executeThing(time2, new ExecutionEvent(4, time2, Duration.ofSeconds(5), "JUnit", -3))

        featurePool.getActiveGridTile().modifyCurrentLocation(time2, 443)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        MusicGridResults firstTile = cmd.playTile()
        print firstTile;

        cmd.nextTile();

        featurePool.getActiveGridTile().executeThing(time2.plusMinutes(20),
                new ExecutionEvent(5, time2.plusMinutes(20), Duration.ofSeconds(5), "JUnit", -2))
        featurePool.getActiveGridTile().finishAfterLoad()

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

        featurePool.getActiveGridTile().startWTF(time1, new CircleDetails(UUID.randomUUID(), "hi"), StartTypeTag.Resume)
        featurePool.getActiveGridTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(9))

        featurePool.getActiveGridTile().modifyCurrentLocation(time1, 100)
        featurePool.getActiveGridTile().modifyCurrentLocation(time2, 443)

        featurePool.getActiveGridTile().modifyCurrentLocation(time4, 20)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        MusicGridResults firstTile = cmd.playTile()
        print firstTile;

        cmd.nextTile();

        featurePool.getActiveGridTile().clearWTF(time3.plusMinutes(20), FinishTypeTag.DoItLater)
        featurePool.getActiveGridTile().finishAfterLoad()

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
    //matrix metrics by project, task, component

    //batch box track?
    //traversal track?

    //metrics per box




//    def "play track SHOULD return text-console output for printing the specified track row"() {
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

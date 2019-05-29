package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd

import com.dreamscale.htmflow.core.domain.member.json.Member
import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.executor.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.executor.machine.TorchiePoolExecutor
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Results
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.FinishCircleTag
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.StartCircleTag
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool
import com.dreamscale.htmflow.core.gridtime.executor.memory.InMemoryFeaturePool
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.AuthorsDetails
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.ExecutionEvent
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.StructureLevel
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName
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
        featurePool = new InMemoryFeaturePool(torchieId);

        torchie = new Torchie(torchieId, featurePool, clockStart);
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
        assert featurePool.getActiveGridTile().gridCoordinates.getFormattedGridTime() == "2019B1-W1-D1_12am+2:00"

    }

    def "goto tile and play track"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        featurePool.getActiveGridTile().startWTF(time3, new StartCircleTag(UUID.randomUUID(), "hi", "resume"))

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetName.IdeaFlow)
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null
        assert trackOutput.toDisplayString().contains("wtf^")
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
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null
        assert trackOutput.toDisplayString().contains("gr^rr")
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
        Results trackOutput = cmd.playTrack(TrackSetName.Navigations)
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null
        assert trackOutput.toDisplayString().contains("abca")
    }

    def "change work contexts and update bands and start/finish tags"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        UUID projA =  UUID.randomUUID();
        UUID taskA = UUID.randomUUID();
        UUID intentionA = UUID.randomUUID();
        UUID taskB = UUID.randomUUID();
        UUID intentionB = UUID.randomUUID();


        featurePool.getActiveGridTile().beginContext(time1, StructureLevel.PROJECT, projA, "projectA");
        featurePool.getActiveGridTile().beginContext(time1, StructureLevel.TASK, taskA, "taskA");
        featurePool.getActiveGridTile().beginContext(time1, StructureLevel.INTENTION, intentionA, "intA");

        featurePool.getActiveGridTile().beginContext(time3, StructureLevel.TASK, taskB, "taskB");
        featurePool.getActiveGridTile().beginContext(time3, StructureLevel.INTENTION, intentionB, "intB");

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTrack(TrackSetName.WorkContext)
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null
        assert trackOutput.toDisplayString().contains("proje*^")
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
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null
        assert trackOutput.toDisplayString().contains("AS ML")
    }

    def "carry over context from one tile to the next"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        Member arty = new Member(UUID.randomUUID().toString(), "Arty Starr");

        featurePool.getActiveGridTile().beginContext(time3, StructureLevel.PROJECT, UUID.randomUUID(), "projA");
        featurePool.getActiveGridTile().startAuthors(time1, new AuthorsDetails(DefaultCollections.toList(arty)));

        featurePool.getActiveGridTile().startFeelsBand(time2.plusMinutes(5), -4)
        featurePool.getActiveGridTile().startWTF(time3, new StartCircleTag(UUID.randomUUID(), "hi", "resume"))

        featurePool.getActiveGridTile().gotoLocation(time2, "/some/placeA", Duration.ofSeconds(9))
        featurePool.getActiveGridTile().gotoLocation(time2, "/some/placeB", Duration.ofSeconds(12))

        featurePool.getActiveGridTile().executeThing(time1, new ExecutionEvent(3, time1, Duration.ofSeconds(1), "JUnit", -3))
        featurePool.getActiveGridTile().executeThing(time2, new ExecutionEvent(4, time2, Duration.ofSeconds(5), "JUnit", -3))

        featurePool.getActiveGridTile().modifyCurrentLocation(time2, 443)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTile()
        print trackOutput.toDisplayString();

        cmd.nextTile();

        featurePool.getActiveGridTile().executeThing(time2.plusMinutes(20), new ExecutionEvent(5, time2.plusMinutes(20), Duration.ofSeconds(5), "JUnit", -2))

        featurePool.getActiveGridTile().finishAfterLoad()

        trackOutput = cmd.playTile()
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null;
        assert trackOutput.toDisplayString().contains("1200")
    }

    def "should create progress band when modification threshold reached"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        featurePool.getActiveGridTile().startWTF(time1, new StartCircleTag(UUID.randomUUID(), "circle", "resume"))
        featurePool.getActiveGridTile().gotoLocation(time1, "/some/placeA", Duration.ofSeconds(9))

        featurePool.getActiveGridTile().modifyCurrentLocation(time1, 100)
        featurePool.getActiveGridTile().modifyCurrentLocation(time2, 443)

        featurePool.getActiveGridTile().modifyCurrentLocation(time4, 20)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        Results trackOutput = cmd.playTile()
        print trackOutput.toDisplayString();

        cmd.nextTile();

        featurePool.getActiveGridTile().clearWTF(time3.plusMinutes(20), new FinishCircleTag(UUID.randomUUID(), "circle", "cancel"))
        featurePool.getActiveGridTile().finishAfterLoad()

        trackOutput = cmd.playTile()
        print trackOutput.toDisplayString();

        then:
        assert trackOutput != null;
        assert trackOutput.toDisplayString().contains("563")
    }


    //so the next things to do...

    //feature resolver, add support for boxes & bridges

    //feeds pull chain, and bookmarking, run this through the pool, write a new test around pull chains
    //save matrix grid

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

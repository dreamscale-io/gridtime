package com.dreamscale.htmflow.core.gridtime.machine.clock

import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.MetronomeJob
import spock.lang.Specification

import java.time.LocalDateTime

class MetronomeSpec extends Specification {

    LocalDateTime clockStart
    MyMetronomeJob metronomeJob

    def setup() {
        clockStart = GeometryClock.getFirstMomentOfYear(2019)
        metronomeJob = new MyMetronomeJob(clockStart)
    }

    def "should tick from beginning without repeating first node"() {
        given:
        Metronome metronome = new Metronome(metronomeJob)

        when:
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()
        metronome.tick()

        then:
        assert metronomeJob.baseTickFroms[0] == "2019-B1-W1-D1_12am+0:00"
        assert metronomeJob.baseTickFroms[1] == "2019-B1-W1-D1_12am+0:20"
        assert metronomeJob.baseTickFroms[2] == "2019-B1-W1-D1_12am+0:40"
        assert metronomeJob.baseTickFroms[3] == "2019-B1-W1-D1_12am+1:00"
        assert metronomeJob.baseTickFroms[4] == "2019-B1-W1-D1_12am+1:20"
        assert metronomeJob.baseTickFroms[5] == "2019-B1-W1-D1_12am+1:40"
        assert metronomeJob.baseTickFroms[6] == "2019-B1-W1-D1_12am+2:00"
        assert metronomeJob.baseTickFroms[7] == "2019-B1-W1-D1_12am+2:20"
        assert metronomeJob.baseTickFroms[8] == "2019-B1-W1-D1_12am+2:40"
        assert metronomeJob.baseTickFroms[9] == "2019-B1-W1-D1_12am+3:00"
        assert metronomeJob.baseTickFroms[10] == "2019-B1-W1-D1_12am+3:20"
        assert metronomeJob.baseTickFroms[11] == "2019-B1-W1-D1_12am+3:40"
        assert metronomeJob.baseTickFroms[12] == "2019-B1-W1-D1_4am+0:00"

        assert metronomeJob.aggregateTickFroms[0] == "2019-B1-W1-D1_12am"
        assert metronomeJob.aggregateTickTos[0] == "2019-B1-W1-D1_4am"
    }

    private class MyMetronomeJob implements MetronomeJob {

        LocalDateTime startPosition

        List<String> baseTickFroms = new ArrayList<>()
        List<String> baseTickTos = new ArrayList<>()

        List<String> aggregateTickFroms = new ArrayList<>()
        List<String> aggregateTickTos = new ArrayList<>()


        MyMetronomeJob(LocalDateTime startPosition) {
            this.startPosition = startPosition;
        }

        @Override
        LocalDateTime getStartPosition() {
            return startPosition
        }

        @Override
        boolean canTick(LocalDateTime nextPosition) {
            return true
        }

        @Override
        void gotoPosition(GeometryClock.GridTime coords) {

        }

        @Override
        TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
            println fromGridTime.coords
            baseTickFroms.add(fromGridTime.formattedGridTime)
            baseTickTos.add(toGridTime.formattedGridTime)

            return null
        }

        @Override
        TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
            aggregateTickFroms.add(fromGridTime.formattedGridTime)
            aggregateTickTos.add(toGridTime.formattedGridTime)

            return null
        }
    }
}

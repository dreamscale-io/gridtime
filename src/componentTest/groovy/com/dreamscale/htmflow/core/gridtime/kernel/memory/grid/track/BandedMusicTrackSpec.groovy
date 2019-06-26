package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock
import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.IdeaFlowStateReference
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.StartTypeTag
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.IdeaFlowStateType
import spock.lang.Specification

import java.time.LocalDateTime

class BandedMusicTrackSpec extends Specification {


    BandedMusicTrack<IdeaFlowStateReference> bandedMusicTrack
    MusicClock musicClock
    FeatureCache featureCache
    GeometryClock.GridTime gridTime

    def setup() {

        featureCache = new FeatureCache();
        gridTime = new GeometryClock(LocalDateTime.now()).getActiveGridTime()
        musicClock = new MusicClock(ZoomLevel.TWENTY)
        bandedMusicTrack = new BandedMusicTrack<>("@row/name", gridTime, musicClock)

        featureCache
    }

    def "should generate band that goes from startPlaying(beat) and rollover to the end"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)

        LocalDateTime moment = gridTime.getMomentFromOffset(musicClock.getBeat(5).getRelativeDuration());

        when:
        bandedMusicTrack.startPlaying(moment, wtfState)
        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert wtfState == bandedMusicTrack.getFeatureAt(8)
        assert wtfState == bandedMusicTrack.getFeatureAt(9)
        assert wtfState == bandedMusicTrack.getFeatureAt(10)
        assert wtfState == bandedMusicTrack.getFeatureAt(20)

        assert rolloverTag != null
        assert rolloverTag.tag == StartTypeTag.Rollover
        assert rolloverTag.feature == wtfState

    }

    def "should generate band that goes from startPlaying(beat) and switch on state switch"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)
        IdeaFlowStateReference progressState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.PROGRESS_STATE)

        LocalDateTime moment5 = gridTime.getMomentFromOffset(musicClock.getBeat(5).getRelativeDuration());
        LocalDateTime moment9 = gridTime.getMomentFromOffset(musicClock.getBeat(9).getRelativeDuration());


        when:
        bandedMusicTrack.startPlaying(moment5, wtfState)
        bandedMusicTrack.startPlaying(moment9, progressState)

        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert wtfState == bandedMusicTrack.getFeatureAt(8)
        assert progressState == bandedMusicTrack.getFeatureAt(9)
        assert progressState == bandedMusicTrack.getFeatureAt(10)
        assert progressState == bandedMusicTrack.getFeatureAt(20)

        assert rolloverTag != null
        assert rolloverTag.tag == StartTypeTag.Rollover
        assert rolloverTag.feature == progressState

    }

    def "should generate band that goes from startPlaying(beat) unit point of clearing"() {
        given:
        IdeaFlowStateReference wtfState = featureCache.lookupIdeaFlowStateReference(IdeaFlowStateType.WTF_STATE)

        LocalDateTime moment5 = gridTime.getMomentFromOffset(musicClock.getBeat(5).getRelativeDuration());
        LocalDateTime moment7 = gridTime.getMomentFromOffset(musicClock.getBeat(7).getRelativeDuration());

        when:
        bandedMusicTrack.startPlaying(moment5, wtfState)
        bandedMusicTrack.stopPlaying(moment7)

        FeatureTag<IdeaFlowStateReference> rolloverTag = bandedMusicTrack.finish()

        then:
        assert wtfState == bandedMusicTrack.getFeatureAt(5)
        assert wtfState == bandedMusicTrack.getFeatureAt(6)
        assert wtfState == bandedMusicTrack.getFeatureAt(7)
        assert null == bandedMusicTrack.getFeatureAt(8)


    }


}

package com.dreamscale.htmflow.core.feeds.story.feature.structure


import spock.lang.Specification

public class ThoughtBubbleSpec extends Specification {

    ThoughtBubble bubble

    def setup() {
        bubble = new ThoughtBubble();
    }

    def "ring locations should organize into radial structure"() {
        given:
        def locationCenter = new LocationInBox("box", "/path/to/file");
        def locationA = new LocationInBox("box", "/path/to/fileA");
        def locationB = new LocationInBox("box", "/path/to/fileB");

        def traversalA = new Traversal(locationCenter, locationA)
        def traversalB = new Traversal(locationCenter, locationB)
        def traversalC = new Traversal(locationA, locationB)

        when:
        bubble.placeCenter(locationCenter)
        bubble.addLocationToFirstRing(locationA, traversalA);
        bubble.addLocationToFirstRing(locationB, traversalB);
        bubble.addExtraLinkWithinFirstRing(locationA, locationB, traversalC)
        bubble.finish();

        def rings = bubble.getRings();

        then:
        assert rings.size() == 1
        assert rings.get(0).ringLocations.size() == 2
        assert rings.get(0).ringLocations.get(0).getSlot() == 0
        assert rings.get(0).ringLocations.get(1).getSlot() == 1
        assert rings.get(0).linksToInnerRing.size() == 2
        assert rings.get(0).linksWithinRing.size() == 1
        assert bubble.getAllLocations().size() == 3;
        assert bubble.getAllTraversals().size() == 3

    }
}

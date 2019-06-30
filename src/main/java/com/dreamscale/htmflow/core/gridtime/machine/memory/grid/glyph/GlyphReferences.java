package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.glyph;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.PlaceType;
import lombok.Getter;

import java.util.Map;

@Getter
public class GlyphReferences {

    private Map<PlaceReference, String> locationGlyphMappings = DefaultCollections.map();
    private char shortHandLocationLetter = 'a';

    private Map<PlaceReference, String> boxGlyphMappings = DefaultCollections.map();
    private char shortHandBoxLetter = 'K'; //need fewer boxes than other things, start from K to Z

    private Map<PlaceReference, String> traversalGlyphMappings = DefaultCollections.map();
    private char shortHandTraversalLetter = 'a';
    private char shortHandBridgeLetter = 'A';

    public void addBoxGlyph(PlaceReference box) {
        String shortHand = boxGlyphMappings.get(box);

        if (shortHand == null) {
            shortHand = String.valueOf(shortHandBoxLetter);
            boxGlyphMappings.put(box, shortHand);
            shortHandBoxLetter++;
        }
    }

    public void addLocationGlyph(PlaceReference locationInBox) {
        String shortHand = locationGlyphMappings.get(locationInBox);

        if (shortHand == null) {
            shortHand = String.valueOf(shortHandLocationLetter);
            locationGlyphMappings.put(locationInBox, shortHand);
            shortHandLocationLetter++;
        }
    }

    public void addTraversalGlyph(PlaceReference traversalReference) {

        String shortHand = traversalGlyphMappings.get(traversalReference);
        if (shortHand == null) {
            if (traversalReference.getPlaceType() == PlaceType.TRAVERSAL_IN_BOX) {
                shortHand = String.valueOf(shortHandTraversalLetter);
                shortHandTraversalLetter++;
            } else if (traversalReference.getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
                shortHand = String.valueOf(shortHandBridgeLetter);
                shortHandBridgeLetter++;
            }

            traversalGlyphMappings.put(traversalReference, shortHand);
        }

    }

}

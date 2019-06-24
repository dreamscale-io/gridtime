package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.Tag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;

import java.util.List;

public interface MusicTrack<F extends FeatureReference> {

    GridRow toGridRow();

    FeatureTag<F> finish();

    F getFirst();

    F getLast();

    F getFeatureAt(int beatNumber);

    F getFeatureAt(RelativeBeat beat);

    List<Tag> getTagsAt(int beatNumber);

    List<Tag> getTagsAt(RelativeBeat beat);

    String getRowName();
}

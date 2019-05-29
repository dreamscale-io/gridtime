package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FeatureTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.Tag;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridCell;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell.GridRow;

import java.util.Iterator;
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

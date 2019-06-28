package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.type;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;

import java.util.List;
import java.util.UUID;

public interface GridCell extends Observable {

    String toHeaderCell();

    String toValueCell();

    String toHeaderCell(int overrideCellSize);

    String toValueCell(int overrideCellSize);

    List<UUID> getFeatureRefs();

    List<FeatureTag<?>> getFeatureTags();

    boolean hasFeature(FeatureReference reference);

    <F extends FeatureReference> F getFeature();
}

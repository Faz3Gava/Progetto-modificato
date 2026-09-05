package com.citylogic.domain.tick;

import com.citylogic.domain.core.CitySnapshot;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Strategy interface for discrete phases of the city simulation tick pipeline.
 * Phases receive a read-only snapshot and grid view and return immutable ResourceDeltas.
 */
public interface ITickPhase {
    ResourceDelta execute(CitySnapshot snapshot, IGridReadPort grid);
}

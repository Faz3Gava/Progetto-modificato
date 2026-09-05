package com.citylogic.domain.map;

import com.citylogic.domain.buildings.BuildingDescription;
import com.citylogic.domain.buildings.IBuildingState;

/**
 * Mutating command port for building construction and demolition on the city grid.
 */
public interface IGridCommandPort {
    IBuildingState constructBuildingAt(int x, int y, BuildingDescription desc);
    IBuildingState removeBuildingAt(int x, int y);
}

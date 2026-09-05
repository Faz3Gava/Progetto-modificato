package com.citylogic.application.policies;

import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.ResourceDelta;
import com.citylogic.domain.map.IGridReadPort;

/**
 * Strategy pattern interface for municipal policies evaluated during the simulation tick.
 */
public interface IPolicyStrategy {
    String getId();
    String getName();
    String getDescription();
    ResourceDelta calculateModifier(IBuildingState building, IGridReadPort grid);
}

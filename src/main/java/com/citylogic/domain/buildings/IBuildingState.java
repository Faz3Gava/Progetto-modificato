package com.citylogic.domain.buildings;

import com.citylogic.domain.core.Point;
import com.citylogic.domain.core.ResourceDelta;

/**
 * Read-only projection interface for a placed building on the city grid.
 */
public interface IBuildingState {
    String getId();
    String getType();
    Point getPosition();
    BuildingDescription getDescription();
    boolean isPowered();
    ResourceDelta getBaseProduction();
    ResourceDelta getCurrentProduction();
    void setPowered(boolean powered);
}

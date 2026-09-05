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

    default boolean isOccupied() {
        return true;
    }

    default boolean IsOccupied() {
        return isOccupied();
    }

    default IBuildingState GetBuilding() {
        return this;
    }

    default IBuildingState getBuilding() {
        return this;
    }

    default int getEnergyProduction() {
        return getDescription() != null ? getDescription().getEnergyProduction() : 0;
    }

    default int getEnergyConsumption() {
        return getDescription() != null ? getDescription().getEnergyConsumption() : 0;
    }

    default int getNetEnergy() {
        return getEnergyProduction() - getEnergyConsumption();
    }

    default boolean hasEnergyAccess() {
        return isPowered();
    }

    default boolean isManualShutdown() {
        return false;
    }

    default void setManualShutdown(boolean manualShutdown) {
    }
}

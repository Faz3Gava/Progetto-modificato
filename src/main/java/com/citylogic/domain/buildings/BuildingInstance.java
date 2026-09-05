package com.citylogic.domain.buildings;

import com.citylogic.domain.core.Point;
import com.citylogic.domain.core.ResourceDelta;

import java.util.UUID;

/**
 * Entity representing an active building located on the city grid.
 */
public class BuildingInstance implements IBuildingState {
    private final String id;
    private final BuildingDescription description;
    private final Point position;
    private boolean powered;
    private boolean manualShutdown = false;
    private double currentMaintenanceCost;

    public BuildingInstance(BuildingDescription description, int x, int y, String id) {
        if (description == null) {
            throw new IllegalArgumentException("BuildingDescription cannot be null");
        }
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.description = description;
        this.position = new Point(x, y);
        this.powered = true;
        this.currentMaintenanceCost = description.getBaseMaintenanceCost();
    }

    public BuildingInstance(BuildingDescription description, int x, int y) {
        this(description, x, y, null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return description.getName();
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public BuildingDescription getDescription() {
        return description;
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public boolean isManualShutdown() {
        return manualShutdown;
    }

    @Override
    public void setManualShutdown(boolean manualShutdown) {
        this.manualShutdown = manualShutdown;
        if (manualShutdown) {
            this.powered = false;
        }
    }

    public double getCurrentMaintenanceCost() {
        return currentMaintenanceCost;
    }

    public void setCurrentMaintenanceCost(double currentMaintenanceCost) {
        this.currentMaintenanceCost = currentMaintenanceCost;
    }

    @Override
    public ResourceDelta getBaseProduction() {
        return description.getBaseProduction();
    }

    @Override
    public int getEnergyProduction() {
        return description.getEnergyProduction();
    }

    @Override
    public int getEnergyConsumption() {
        return description.getEnergyConsumption();
    }

    @Override
    public int getNetEnergy() {
        return description.getNetEnergy();
    }

    @Override
    public boolean hasEnergyAccess() {
        return powered;
    }

    @Override
    public ResourceDelta getCurrentProduction() {
        if (!powered) {
            return ResourceDelta.zero();
        }
        return description.getBaseProduction();
    }

    @Override
    public boolean isOccupied() {
        return true;
    }

    @Override
    public boolean IsOccupied() {
        return isOccupied();
    }

    @Override
    public BuildingInstance GetBuilding() {
        return this;
    }

    @Override
    public BuildingInstance getBuilding() {
        return this;
    }

    @Override
    public String toString() {
        return description.getName() + " at " + position + " (ID: " + id.substring(0, Math.min(8, id.length())) + ")";
    }
}

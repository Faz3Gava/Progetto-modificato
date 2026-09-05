package com.citylogic.domain.map;

import com.citylogic.domain.buildings.BuildingInstance;
import com.citylogic.domain.core.Point;

/**
 * Individual tile/cell in the city matrix.
 */
public class Cell {
    private final int x;
    private final int y;
    private final Point position;
    private double pollutionLevel;
    private BuildingInstance currentBuilding;
    public boolean isOccupied = false;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.position = new Point(x, y);
        this.pollutionLevel = 0.0;
        this.currentBuilding = null;
        this.isOccupied = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point getPosition() {
        return position;
    }

    public boolean isOccupied() {
        return currentBuilding != null;
    }

    public boolean IsOccupied() {
        return isOccupied();
    }

    public BuildingInstance getBuilding() {
        return currentBuilding;
    }

    public BuildingInstance GetBuilding() {
        return currentBuilding;
    }

    public void setBuilding(BuildingInstance building) {
        this.currentBuilding = building;
        this.isOccupied = (building != null);
    }

    public void SetBuilding(BuildingInstance building) {
        setBuilding(building);
    }

    public void clear() {
        this.currentBuilding = null;
        this.isOccupied = false;
    }

    public void Clear() {
        clear();
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        this.pollutionLevel = Math.max(0.0, pollutionLevel);
    }
}

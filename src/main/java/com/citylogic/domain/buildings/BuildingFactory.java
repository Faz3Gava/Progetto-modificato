package com.citylogic.domain.buildings;

/**
 * Factory pattern creating BuildingInstance entities from Flyweight descriptions.
 */
public class BuildingFactory {
    public BuildingInstance createBuilding(BuildingDescription description, int x, int y) {
        return new BuildingInstance(description, x, y);
    }

    public BuildingInstance createBuilding(BuildingDescription description, int x, int y, String id) {
        return new BuildingInstance(description, x, y, id);
    }
}

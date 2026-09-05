package com.citylogic.domain.map;

import com.citylogic.domain.buildings.IBuildingState;
import com.citylogic.domain.core.Dimension;
import com.citylogic.domain.core.Point;

import java.util.List;
import java.util.Optional;

/**
 * Read-only port for querying the city spatial grid and buildings.
 */
public interface IGridReadPort {
    String getTerrainAt(int x, int y);
    Optional<IBuildingState> getBuildingById(String id);
    List<IBuildingState> getAllBuildings();
    List<IBuildingState> getAdjacentBuildings(String id, int radius);
    boolean isAreaFree(int x, int y, Dimension footprint);
    Dimension getDimensions();
    boolean isWithinBounds(int x, int y);

    boolean isOccupied(int x, int y);

    default boolean isOccupied(Point point) {
        return point != null && isOccupied(point.getX(), point.getY());
    }

    default boolean IsOccupied(int x, int y) {
        return isOccupied(x, y);
    }

    default boolean IsOccupied(Point point) {
        return isOccupied(point);
    }

    IBuildingState getBuilding(int x, int y);

    default IBuildingState GetBuilding(int x, int y) {
        return getBuilding(x, y);
    }

    default IBuildingState getBuilding(Point point) {
        return point != null ? getBuilding(point.getX(), point.getY()) : null;
    }

    default IBuildingState GetBuilding(Point point) {
        return getBuilding(point);
    }

    default Optional<IBuildingState> getBuildingAt(int x, int y) {
        return Optional.ofNullable(getBuilding(x, y));
    }

    default Optional<IBuildingState> GetBuildingAt(int x, int y) {
        return getBuildingAt(x, y);
    }
}
